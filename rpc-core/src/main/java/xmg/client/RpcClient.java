package xmg.client;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.lang.NonNull;
import xmg.client.providers.Provider;
import xmg.client.proxy.JdkProxy;
import xmg.client.support.RpcApi;
import xmg.client.support.RpcApiScanner;
import xmg.utils.StringUtils;

import java.util.HashSet;
import java.util.Set;


public class RpcClient implements BeanFactoryPostProcessor, EnvironmentAware {
    final Logger log = LoggerFactory.getLogger(RpcClient.class);
    public static String TOKEN;
    public static final Set<Provider> needRegisteredRpcProviders = new HashSet<>();
    private Environment environment;

    public RpcClient() {
    }

    public void postProcessBeanFactory(@NonNull ConfigurableListableBeanFactory beanFactory) throws BeansException {
        String packages = environment.getProperty("rpc.client.remote-api-packages", String.class);
        RpcClient.TOKEN = environment.getProperty("rpc.client.token", String.class);
        if (packages == null) {
            log.warn("remote api packages is not specified , use the default value \"com.\"");
            packages = "com.";
        }
        log.info("begin scan remote api packages :" + packages);
        try {
            String[] pack = packages.split(",");
            final RpcApiScanner scanner = new RpcApiScanner((BeanDefinitionRegistry) beanFactory, false);
            scanner.addIncludeFilter(new AnnotationTypeFilter(RpcApi.class));
            final Set<BeanDefinitionHolder> beanDefinitionHolders = scanner.doScan(pack);
            for (BeanDefinitionHolder defined : beanDefinitionHolders) {
                final BeanDefinition beanDefinition = defined.getBeanDefinition();
                final String beanClassName = beanDefinition.getBeanClassName();
                final Class<?> aClass = Class.forName(beanClassName);
                final String beanName = defined.getBeanName();
                final RpcApi rpcApi = aClass.getAnnotation(RpcApi.class);
                if (rpcApi != null) {
                    final Object proxyInstance = JdkProxy.getInstance().getProxyInstance(aClass);
                    beanFactory.registerSingleton(beanName, proxyInstance);
                    final String name = rpcApi.value();
                    final String url = rpcApi.url();
                    final boolean trace = rpcApi.trace();
                    final Provider provider = new Provider();
                    provider.setTrace(trace);
                    if (StringUtils.isNotBlank(url)) {
                        final String[] split = url.split(":");
                        provider.setHost(split[0]);
                        provider.setPort(Integer.parseInt(split[1]));
                        if (provider.getInetAddress() == null) {
                            throw new RuntimeException(beanClassName + " url error");
                        }
                        needRegisteredRpcProviders.add(provider);
                    } else if (StringUtils.isNotBlank(name)) {
                        provider.setName(name);
                        needRegisteredRpcProviders.add(provider);
                    } else {
                        throw new RuntimeException(beanClassName + " not hava a url or name");
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void setEnvironment(@NonNull Environment environment) {
        this.environment = environment;
    }
}
