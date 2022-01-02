package xmg.client.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xmg.client.connect.ConnectionManager;
import xmg.client.connect.exception.RemoteApiException;
import xmg.client.handler.ClientHandler;
import xmg.client.support.RpcApi;
import xmg.codec.Request;
import xmg.server.RpcServer;
import xmg.utils.StringUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class JdkProxy {
    private static final Logger log = LoggerFactory.getLogger(RpcServer.class);
    private static final ThreadLocal<Request> threadLocal = new ThreadLocal<>();

    private static final JdkProxy proxy = new JdkProxy();

    public static JdkProxy getInstance() {
        return proxy;
    }

    private JdkProxy() {
    }

    @SuppressWarnings("unchecked")
    public <T> T getProxyInstance(Class<T> interfaceClass) {
        final Object o = Proxy.newProxyInstance(interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                new Handler(interfaceClass));
        return (T) o;
    }

    static class Handler implements InvocationHandler {
        private final Class<?> target;

        Handler(Class<?> target) {
            this.target = target;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws InterruptedException {
            final RpcApi rpcApi = target.getAnnotation(RpcApi.class);
            if (rpcApi == null) {
                throw new RuntimeException("is not rpcApi");
            }
            final ConnectionManager connectionManager = ConnectionManager.getInstance();
            final String name = rpcApi.value();
            final String url = rpcApi.url();
            ClientHandler handler;
            if (StringUtils.isNotBlank(name)) {
                handler = connectionManager.choiceHandler(name);
            } else if (StringUtils.isNotBlank(url)) {
                final String[] split = url.split(":");
                handler = connectionManager.choiceHandler(split[0], Integer.parseInt(split[1]));
            } else {
                throw new RemoteApiException("not definition server provider");
            }
            if (handler == null) {
                throw new RemoteApiException("can not find server provider");
            }
            final Request request = new Request(method, args);
            final Request parentRequest = threadLocal.get();
            if (parentRequest!=null) {
                request.setParentRequestId(parentRequest.getRequestId());
                request.setTrace(parentRequest.isTrace());
            }
            return handler.senRequest(request).get();
        }
    }
}
