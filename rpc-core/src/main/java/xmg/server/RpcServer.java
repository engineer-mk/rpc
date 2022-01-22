package xmg.server;


import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import xmg.codec.Response;
import xmg.server.handler.ServerInitializer;
import xmg.server.support.MethodInfo;
import xmg.server.support.ServerMethod;
import xmg.server.support.annotation.RpcProvider;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public class RpcServer implements ApplicationContextAware {
    private static final Logger log = LoggerFactory.getLogger(RpcServer.class);
    public static String TOKEN;
    private static final Map<String, Response> RESPONSE_MAP = new ConcurrentHashMap<>();
    public static final Set<String> IGNORE_EXCEPTIONS = new HashSet<>();

    private final NioEventLoopGroup bossGroup = new NioEventLoopGroup(3);
    private final NioEventLoopGroup workGroup = new NioEventLoopGroup();
    private final Map<MethodInfo, ServerMethod> serviceMethodMap = new HashMap<>();
    private ApplicationContext ctx;
    private final Integer port;

    public RpcServer(Integer port) {
        this.port = port;
    }

    @PostConstruct
    public void initServer() {
        new ServerBootstrap()
                .group(bossGroup, workGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 100)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(new ServerInitializer(this))
                .bind(port)
                .addListener((ChannelFutureListener) cf -> {
                    if (cf.isSuccess()) {
                        log.info("RpcServer started in port:{}", port);
                    } else {
                        log.error("Failed to start rpcServer");
                    }
                });
    }

    @PreDestroy
    public void stop() {
        bossGroup.shutdownGracefully();
        workGroup.shutdownGracefully();
    }

    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        this.ctx = ctx;
        final Map<String, Object> beans = ctx.getBeansWithAnnotation(RpcProvider.class);
        beans.forEach((name, o) -> {
            final Class<?> aClass = o.getClass().getSuperclass();
            final Method[] methods = aClass.getDeclaredMethods();
            for (Method m : methods) {
                if (m.getModifiers() == Modifier.PUBLIC) {
                    final MethodInfo methodInfo = new MethodInfo(m.getName(), m.getParameterTypes());
                    if (serviceMethodMap.containsKey(methodInfo)) {
                        final ServerMethod method = serviceMethodMap.get(methodInfo);
                        String meg = aClass.getName() + ":" + m.getName() + " has been exist in bean " + method.toString();
                        throw new RuntimeException(meg);
                    }
                    serviceMethodMap.put(methodInfo, new ServerMethod(name, m));
                }
            }
        });
    }

    public ServerMethod getServerMethod(MethodInfo key) {
        return serviceMethodMap.get(key);
    }

    public Object getBean(String name) {
        return ctx.getBean(name);
    }

    public static Response getResponse(String requestId) {
        return RESPONSE_MAP.get(requestId);
    }

    public static void putResponse(String requestId, Response response) {
        RESPONSE_MAP.put(requestId, response);
    }

    public static void removeResponse(String requestId) {
        RESPONSE_MAP.remove(requestId);
    }
}
