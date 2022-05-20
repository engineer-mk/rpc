package xmg.client.connect;


import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import xmg.client.RpcClient;
import xmg.client.connect.loadbalance.RpcLoadBalance;
import xmg.client.connect.loadbalance.impl.RoundRpcLoadBalance;
import xmg.client.handler.ClientHandler;
import xmg.client.handler.ClientInitializer;
import xmg.client.providers.Provider;
import xmg.client.proxy.JdkProxy;
import xmg.client.support.Client;
import xmg.client.support.RpcApi;
import xmg.utils.StringUtils;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class ConnectionManager implements InitializingBean, DisposableBean {
    private static final Logger log = LoggerFactory.getLogger(ConnectionManager.class);

    private final EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
    private final Map<Provider, ClientHandler> connectedServerNodes = new ConcurrentHashMap<>();
    private final Set<Provider> connectedServerProvider = new CopyOnWriteArraySet<>();
    private static final ConnectionManager instance = new ConnectionManager();
    private static final ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
    private volatile boolean isRunning;
    private final RpcLoadBalance loadBalance = new RoundRpcLoadBalance();

    public static ConnectionManager getInstance() {
        return instance;
    }


    private ConnectionManager() {
    }

    @Nullable
    public ClientHandler choiceOneHandler(Integer nodeId) {
        if (!isRunning || connectedServerNodes.isEmpty()) {
            return null;
        }
        final Optional<Provider> optional = connectedServerNodes.keySet()
                .stream()
                .filter(it -> nodeId.equals(it.getId()))
                .findAny();
        return optional.map(connectedServerNodes::get).orElse(null);
    }

    @Nullable
    public ClientHandler choiceOneHandler(String providerName) {
        if (!isRunning || connectedServerNodes.isEmpty()) {
            return null;
        }
        final List<Provider> providers = new ArrayList<>(connectedServerNodes.keySet());
        final Provider choose = loadBalance.choose(providerName, providers);
        return connectedServerNodes.get(choose);
    }

    @Nullable
    public ClientHandler choiceOneHandler(@NonNull String host, @NonNull Integer port) {
        if (!isRunning || connectedServerNodes.isEmpty()) {
            return null;
        }
        final Optional<Provider> optional = connectedServerNodes.keySet()
                .stream().filter(it -> host.equals(it.getHost()) && port.equals(it.getPort()))
                .findAny();
        return optional.map(connectedServerNodes::get).orElse(null);
    }


    public void updateServerNodes(Collection<Provider> list) {
        if (!isRunning) {
            return;
        }
        final Set<Provider> providers = connectedServerNodes.keySet();
        for (Provider p : list) {
            if (!providers.contains(p)) {
                doConnectServerNode(p);
            }
        }
        for (Provider p : providers) {
            if (!list.contains(p)) {
                doDisconnectServerNode(p);
            }
        }
    }

    private synchronized void doConnectServerNode(Provider provider) {
        if (connectedServerProvider.contains(provider)) {
            return;
        }
        final InetSocketAddress remoteAddress = provider.getInetAddress();
        if (remoteAddress == null) {
            return;
        }
        log.info("开始注册节点:{}", provider.getInetAddress().toString() + "(" + provider.getName() + ")");
        connectedServerProvider.add(provider);
        final ChannelFuture channelFuture = new Bootstrap()
                .group(eventLoopGroup)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .channel(NioSocketChannel.class)
                .handler(new ClientInitializer(provider))
                .connect(remoteAddress);
        channelFuture.addListener((ChannelFutureListener) cf -> {
            if (cf.isSuccess()) {
                final ClientHandler handler = cf.channel().pipeline().get(ClientHandler.class);
                addProvider(provider, handler);
            } else {
                connectedServerProvider.remove(provider);
                log.error("Failed to connect remote server {}", remoteAddress);
            }
        });
        channelFuture.channel().closeFuture()
                .addListener((ChannelFutureListener) cf -> {
                    if (cf.isSuccess()) {
                        removeProvider(provider);
                        log.warn(provider + " is destroyed !");
                    }
                });
    }

    private synchronized void doDisconnectServerNode(Provider provider) {
        final ClientHandler handler = connectedServerNodes.get(provider);
        if (handler == null) {
            return;
        }
        log.info("开始注销节点:{}", provider.getInetAddress().toString() + "(" + provider.getName() + ")");
        handler.getContext()
                .close()
                .addListener((ChannelFutureListener) cf -> {
                    if (cf.isSuccess()) {
                        removeProvider(provider);
                    } else {
                        log.error("Failed to destroy remote server {}", provider.getInetAddress().toString());
                    }
                });
    }


    @Override
    public void afterPropertiesSet() {
        try {
            isRunning = true;
            updateConnection();
            service.scheduleAtFixedRate(this::updateConnection, 10, 5, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopUpdateConnectionTask() {
        service.shutdownNow();
    }

    private void updateConnection() {
        final Set<Provider> providers = RpcClient.NEED_REGISTERED_RPC_PROVIDERS;
        final List<Provider> list = providers.stream()
                .filter(it -> StringUtils.isBlank(it.getName()))
                .collect(Collectors.toList());
        this.updateServerNodes(list);
    }

    @Override
    public void destroy() {
        isRunning = false;
        service.shutdownNow();
        connectedServerNodes.keySet().forEach(this::doDisconnectServerNode);
    }

    private void addProvider(Provider provider, ClientHandler handler) {
        this.connectedServerNodes.put(provider, handler);
        updateClients(this.connectedServerNodes);
    }

    public void removeProvider(Provider provider) {
        this.connectedServerNodes.remove(provider);
        this.connectedServerProvider.remove(provider);
        updateClients(this.connectedServerNodes);
    }

    private void updateClients(Map<Provider, ClientHandler> connectedServerNodes) {
        final Set<Provider> connectedProviders = connectedServerNodes.keySet();
        final JdkProxy jdkProxy = JdkProxy.getInstance();
        for (final Map.Entry<Class<?>, Set<Client>> entry : RpcClient.CLIENTS_MAP.entrySet()) {
            final Class<?> aClass = entry.getKey();
            final RpcApi rpcApi = aClass.getAnnotation(RpcApi.class);
            final String name = RpcClient.resolverValue(rpcApi.value(), RpcClient.environment);
            final Set<Client> clients = entry.getValue();
            //已经添加的节点
            final List<Provider> registeredProviders = clients
                    .stream().map(Client::getProvider)
                    .collect(Collectors.toList());
            //现存同名节点
            final List<Provider> thisNameProviders = connectedProviders.stream()
                    .filter(it -> name.equals(it.getName()))
                    .collect(Collectors.toList());
            //添加未注册的
            thisNameProviders.forEach(it -> {
                final boolean isRegistered = registeredProviders.contains(it);
                if (!isRegistered) {
                    final Client client = new Client();
                    client.setProvider(it);
                    client.setProxy(jdkProxy.getProxyInstance(aClass, connectedServerNodes.get(it)));
                    clients.add(client);
                }
            });
            //移除过期的
            registeredProviders.forEach(it -> {
                final boolean isPast = !thisNameProviders.contains(it);
                if (isPast) {
                    clients.removeIf(client -> client.getProvider().equals(it));
                }
            });
        }
    }
}
