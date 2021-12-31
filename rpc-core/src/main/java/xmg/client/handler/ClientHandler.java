package xmg.client.handler;


import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xmg.client.providers.Provider;
import xmg.codec.Request;
import xmg.codec.Response;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class ClientHandler extends SimpleChannelInboundHandler<Response> {
    private static final Logger log = LoggerFactory.getLogger(ClientHandler.class);

    private ChannelHandlerContext context;
    private final Map<String, RpcFuture> futureMap = new ConcurrentHashMap<>();
    private final Provider provider;

    public ClientHandler(Provider provider) {
        this.provider = provider;
    }

    public RpcFuture senRequest(Request request) {
        if (log.isDebugEnabled()) {
            log.debug("远程调用--->地址:" + provider.toString() + "方法:" + request.toString());
        }
        final RpcFuture future = new RpcFuture(request);
        try {
            context.writeAndFlush(request).sync();
            final String requestId = request.getRequestId();
            futureMap.put(requestId, future);
        } catch (InterruptedException e) {
            log.error(e.getLocalizedMessage(), e);
        }
        return future;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Response response) {
        String requestId = response.getRequestId();
        RpcFuture rpcFuture = futureMap.get(requestId);
        if (rpcFuture != null) {
            futureMap.remove(requestId);
            rpcFuture.done(response);
        } else {
            log.warn("request id error: " + requestId);
        }
        futureMap.forEach((s, rf) -> {
            if (rf.isCancelled()) {
                futureMap.remove(s);
            }
        });
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        log.info(provider.getInetAddress().toString() + " initialization succeeded!");
        this.context = ctx;
        super.handlerAdded(ctx);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        log.info(provider.getInetAddress().toString() + " destroyed!");
        super.handlerRemoved(ctx);
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("远程调用异常:", cause);
    }

    public ChannelHandlerContext getContext() {
        return context;
    }
}
