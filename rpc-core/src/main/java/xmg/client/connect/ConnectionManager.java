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
import xmg.utils.StringUtils;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ConnectionManager implements InitializingBean, DisposableBean {
    private static final Logger log = LoggerFactory.getLogger(ConnectionManager.class);

    private final EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
    private final Map<Provider, ClientHandler> connectedServerNodes = new ConcurrentHashMap<>();
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
        if (connectedServerNodes.containsKey(provider)) {
            return;
        }
        final InetSocketAddress remoteAddress = provider.getInetAddress();
        if (remoteAddress == null) {
            return;
        }
        connectedServerNodes.put(provider, new ClientHandler(null));
        final ChannelFuture channelFuture = new Bootstrap()
                .group(eventLoopGroup)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .channel(NioSocketChannel.class)
                .handler(new ClientInitializer(provider))
                .connect(remoteAddress);
        channelFuture.addListener((ChannelFutureListener) cf -> {
            if (cf.isSuccess()) {
                final ClientHandler handler = cf.channel().pipeline().get(ClientHandler.class);
                connectedServerNodes.put(provider, handler);
            } else {
                connectedServerNodes.remove(provider);
                log.error("Failed to connect remote server {}", remoteAddress.toString());
            }
        });
        channelFuture.channel().closeFuture()
                .addListener((ChannelFutureListener) cf -> {
                    if (cf.isSuccess()) {
                        connectedServerNodes.remove(provider);
                        log.warn(provider + " is destroyed !");
                    }
                });
    }

    private synchronized void doDisconnectServerNode(Provider provider) {
        final ClientHandler handler = connectedServerNodes.get(provider);
        if (handler == null) {
            return;
        }
        handler.getContext()
                .close()
                .addListener((ChannelFutureListener) cf -> {
                    if (cf.isSuccess()) {
                        connectedServerNodes.remove(provider);
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
        connectedServerNodes.keySet().forEach(this::doConnectServerNode);
    }
}
