package xmg.server.handler;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import xmg.codec.Request;
import xmg.codec.Response;
import xmg.codec.RpcDecoder;
import xmg.codec.RpcEncoder;
import xmg.codec.serializer.impl.FastJsonSerializer;
import xmg.server.RpcServer;

public class ServerInitializer extends ChannelInitializer<SocketChannel> {
    private final RpcServer rpcServer;

    public ServerInitializer(RpcServer rpcServer) {
        this.rpcServer = rpcServer;
    }

    @Override
    protected void initChannel(SocketChannel ch) {
        final ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new RpcEncoder(Response.class, FastJsonSerializer.getInstance()));
        pipeline.addLast(new RpcDecoder(Request.class, FastJsonSerializer.getInstance()));
        pipeline.addLast(new ServerHandler(rpcServer));
    }
}
