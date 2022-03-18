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
import xmg.client.support.Client;
import xmg.client.support.RpcApi;
import xmg.client.support.RpcApiScanner;
import xmg.utils.StringUtils;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class RpcClient implements BeanFactoryPostProcessor, EnvironmentAware {
    final Logger log = LoggerFactory.getLogger(RpcClient.class);
    public static String TOKEN;
    public static final Set<String> IGNORE_EXCEPTIONS = new HashSet<>();
    public static final Set<Provider> NEED_REGISTERED_RPC_PROVIDERS = new HashSet<>();
    public static final Map<Class<?>, Set<Client>> CLIENTS_MAP = new HashMap<>();
    public static Environment environment;

    public RpcClient() {
    }

    public void postProcessBeanFactory(@NonNull ConfigurableListableBeanFactory beanFactory) throws BeansException {
        String packages = environment.getProperty("rpc.client.remote-api-packages", String.class);
        RpcClient.TOKEN = environment.getProperty("rpc.client.token", String.class);
        final String ignoreExceptions = environment.getProperty("rpc.client.ignore-exceptions", String.class);
        if (StringUtils.isNotBlank(ignoreExceptions)) {
            final String[] split = ignoreExceptions.split(",");
            RpcClient.IGNORE_EXCEPTIONS.addAll(Arrays.asList(split));
        }
        if (packages == null) {
            log.warn("remote api packages is not specified , use the default value \"com\"");
            packages = "com";
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
                    final JdkProxy instance = JdkProxy.getInstance();
                    if (JdkProxy.getEnvironment() == null) {
                        instance.setEnvironment(environment);
                    }
                    final Object proxyInstance = instance.getProxyInstance(aClass);
                    //register bean
                    beanFactory.registerSingleton(beanName, proxyInstance);
                    final String name = resolverValue(rpcApi.value(), environment);
                    final String url = resolverValue(rpcApi.url(), environment);
                    final String trace = resolverValue(rpcApi.trace(), environment);
                    final boolean trace0 = "true".equals(trace);
                    final Provider provider = new Provider();
                    provider.setTrace(trace0);
                    if (StringUtils.isNotBlank(url)) {
                        final String[] split = url.split(":");
                        provider.setHost(split[0]);
                        provider.setPort(Integer.parseInt(split[1]));
                        if (provider.getInetAddress() == null) {
                            throw new RuntimeException(beanClassName + " url error");
                        }
                        NEED_REGISTERED_RPC_PROVIDERS.add(provider);
                    } else if (StringUtils.isNotBlank(name)) {
                        provider.setName(name);
                        NEED_REGISTERED_RPC_PROVIDERS.add(provider);
                        CLIENTS_MAP.putIfAbsent(aClass, new CopyOnWriteArraySet<>());
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
        RpcClient.environment = environment;
    }

    private static final Pattern humpPattern = Pattern.compile("[A-Z]");

    private static String formatString(String str) {
        Matcher matcher = humpPattern.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, "-" + matcher.group(0).toLowerCase());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public static String resolverValue(String value, Environment environment) {
        if (value == null || !value.startsWith("$") || environment == null) {
            return value;
        }
        final String str = value.replaceAll("\\$", "");
        final String sub = str.substring(1, value.length() - 2);

        String result = environment.getProperty(sub, String.class);
        if (result == null) {
            result = environment.getProperty(formatString(sub), String.class);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public <T> Map<Provider, T> getClients(Class<T> tClass) {
        final Set<Client> clients = CLIENTS_MAP.get(tClass);
        return clients.stream()
                .collect(Collectors.toMap(Client::getProvider, it -> (T) it.getProxy()));
    }

}
