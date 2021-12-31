package xmg.server.handler;


import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.NettyRuntime;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xmg.client.connect.exception.RemoteAccessException;
import xmg.codec.Request;
import xmg.codec.Response;
import xmg.server.RpcServer;
import xmg.server.support.MethodInfo;
import xmg.server.support.ServerMethod;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class ServerHandler extends SimpleChannelInboundHandler<Request> {
    private static final Logger log = LoggerFactory.getLogger(ServerHandler.class);
    private static final Set<Channel> onLineClients = new CopyOnWriteArraySet<>();
    private final static EventExecutorGroup executor = new DefaultEventExecutorGroup(NettyRuntime.availableProcessors() * 2);
    private final RpcServer rpcServer;

    public ServerHandler(RpcServer rpcServer) {
        this.rpcServer = rpcServer;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Request request) {
        final String requestId = request.getRequestId();
        final Response response = new Response();
        response.setRequestId(requestId);
        final Object[] args = request.getParameters();
        final MethodInfo key = new MethodInfo(request.getMethodName(), request.getParameterTypes());
        final ServerMethod serverMethod = rpcServer.getServerMethod(key);
        if (serverMethod == null) {
            response.setException(new RemoteAccessException("can't find this method " + key.getMethodName()));
            response.setStates(Response.States.NOT_FOUND);
            ctx.writeAndFlush(response);
        } else {
            executor.execute(() -> {
                try {
                    final Object bean = rpcServer.getBean(serverMethod.getBeanName());
                    final Method method = serverMethod.getMethod();
                    final Object result = method.invoke(bean, args);
                    response.setResult(result);
                    response.setStates(Response.States.OK);
                } catch (Exception e) {
                    response.setException(e);
                    response.setStates(Response.States.INTERNAL_SERVER_ERROR);
                }
                ctx.writeAndFlush(response);
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
}
