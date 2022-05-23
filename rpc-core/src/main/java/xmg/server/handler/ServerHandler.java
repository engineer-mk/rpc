package xmg.server.handler;


import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.NettyRuntime;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import io.seata.core.context.RootContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;
import xmg.codec.Request;
import xmg.codec.Response;
import xmg.codec.exception.RPcRemoteAccessException;
import xmg.server.RpcServer;
import xmg.server.support.MethodInfo;
import xmg.server.support.ServerMethod;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class ServerHandler extends SimpleChannelInboundHandler<Request> {
    private static final Logger log = LoggerFactory.getLogger(ServerHandler.class);
    private static final Set<Channel> onLineClients = new CopyOnWriteArraySet<>();
    private final static EventExecutorGroup executor = new DefaultEventExecutorGroup(NettyRuntime.availableProcessors() * 2);
    public static final ThreadLocal<Request> threadLocal = new ThreadLocal<>();
    private final RpcServer rpcServer;

    public ServerHandler(RpcServer rpcServer) {
        this.rpcServer = rpcServer;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Request request) {
        final StopWatch stopWatch = new StopWatch("channelRead0");
        stopWatch.start("preprocessing");
        if (RpcServer.TOKEN != null) {
            final String token = request.getToken();
            if (!RpcServer.TOKEN.equals(token)) {
                ctx.close();
                log.error(ctx.pipeline().channel().remoteAddress() + " token error!");
            }
        }
        final String requestId = request.getRequestId();
        final Response response = new Response(requestId);
        response.setAddress(ctx.channel().localAddress().toString());
        final boolean openTrace = request.isTrace();
        if (openTrace) {
            response.setRequest(request);
        }
        final MethodInfo key = new MethodInfo(request.getMethodName(), request.getParameterTypes());
        final ServerMethod serverMethod = rpcServer.getServerMethod(key);
        if (serverMethod == null) {
            final RPcRemoteAccessException e = new RPcRemoteAccessException("can't find this method " + key.getMethodName(), new UnsupportedOperationException());
            response.setThrowable(e);
            response.setState(Response.State.NOT_FOUND);
            ctx.writeAndFlush(response);
        } else {
            executor.execute(() -> {
                if (openTrace) {
                    RpcServer.putResponse(requestId, response);
                    threadLocal.set(request);
                }
                try {
                    final Object bean = rpcServer.getBean(serverMethod.getBeanName());
                    final Method method = serverMethod.getMethod();
                    final Object[] args = request.getParameters();
                    final String xid = request.getXid();
                    try {
                        Class.forName("io.seata.core.context.RootContext");
                        RootContext.bind(xid);
                    } catch (ClassNotFoundException ignore) {
                    }
                    stopWatch.stop();
                    stopWatch.start("invoke method");
                    final Object result = method.invoke(bean, args);
                    response.setResult(result);
                    response.setState(Response.State.OK);
                } catch (InvocationTargetException e) {
                    Throwable targetException = e.getTargetException();
                    if (targetException instanceof RPcRemoteAccessException) {
                        targetException = ((RPcRemoteAccessException) targetException).getTarget();
                    } else {
                        final String mes = requestId + " 请求异常:" + targetException.toString();
                        if (RpcServer.IGNORE_EXCEPTIONS.contains(targetException.getClass().getName())) {
                            log.warn(mes);
                        } else {
                            log.error(mes, targetException);
                        }
                    }
                    response.setThrowable(targetException);
                    response.setState(Response.State.INTERNAL_SERVER_ERROR);
                } catch (IllegalAccessException e) {
                    log.error(requestId + " 请求异常:" + e.getLocalizedMessage(), e);
                    response.setThrowable(e);
                    response.setState(Response.State.INTERNAL_SERVER_ERROR);
                } finally {
                    if (openTrace) {
                        threadLocal.remove();
                        RpcServer.removeResponse(requestId);
                    }
                }
                stopWatch.stop();
                stopWatch.start("writeAndFlush");
                ctx.writeAndFlush(response);
                stopWatch.stop();
                System.out.println(stopWatch.prettyPrint());
                System.out.println("total ms " + stopWatch.getTotalTimeMillis());
            });
        }
    }


    @Override
    public void channelRegistered(ChannelHandlerContext ctx) {
        final Channel channel = ctx.channel();
        onLineClients.add(channel);
        if (log.isDebugEnabled()) {
            final String remoteAddress = channel.remoteAddress().toString();
            log.debug(remoteAddress + " 连接成功，当前连接数量: " + onLineClients.size());
        }
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) {
        final Channel channel = ctx.channel();
        onLineClients.remove(channel);
        if (log.isDebugEnabled()) {
            final String remoteAddress = channel.remoteAddress().toString();
            log.debug(remoteAddress + " 断开连接，当前连接数量: " + onLineClients.size());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("异常:", cause);
    }
}
