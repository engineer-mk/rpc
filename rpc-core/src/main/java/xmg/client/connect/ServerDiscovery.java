package xmg.client.connect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import xmg.client.RpcClient;
import xmg.client.providers.Provider;
import xmg.utils.StringUtils;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ServerDiscovery implements InitializingBean, DisposableBean {
    private static final Logger log = LoggerFactory.getLogger(ConnectionManager.class);
    private static final ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
    private final DiscoveryClient discoveryClient;
    private final ConnectionManager connectionManager;

    public ServerDiscovery(DiscoveryClient discoveryClient, ConnectionManager connectionManager) {
        this.discoveryClient = discoveryClient;
        this.connectionManager = connectionManager;
    }

    private List<Provider> getServerList(Collection<Provider> needRegisteredRpcProviders) {
        List<Provider> providers = new ArrayList<>();
        for (Provider p : needRegisteredRpcProviders) {
            final String name = p.getName();
            final InetSocketAddress inetAddress = p.getInetAddress();
            if (StringUtils.isNotBlank(name)) {
                final List<ServiceInstance> list = discoveryClient.getInstances(name);
                for (ServiceInstance instance : list) {
                    final Map<String, String> metadata = instance.getMetadata();
                    final Provider provider = new Provider();
                    provider.setName(name);
                    provider.setHost(instance.getHost());
                    final String port = metadata.get("rpcServerPort");
                    provider.setPort(Integer.parseInt(port));
                    providers.add(provider);
                }
            } else if (inetAddress != null) {
                providers.add(p);
            }
        }
        return providers;
    }


    @Override
    public void afterPropertiesSet() {
        try {
            connectionManager.stopUpdateConnectionTask();
            updateConnection();
            service.scheduleAtFixedRate(this::updateConnection, 60, 10, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateConnection() {
        final Set<Provider> providers = RpcClient.needRegisteredRpcProviders;
        final List<Provider> serverList = getServerList(providers);
        connectionManager.updateServerNodes(serverList);
    }

    @Override
    public void destroy() {
        service.shutdownNow();
    }
}
