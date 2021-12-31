package xmg.client.handler;


import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import xmg.client.providers.Provider;
import xmg.codec.Request;
import xmg.codec.Response;
import xmg.codec.RpcDecoder;
import xmg.codec.RpcEncoder;
import xmg.codec.serializer.impl.FastJsonSerializer;


public class ClientInitializer extends ChannelInitializer<SocketChannel> {
    private final Provider provider;

    public ClientInitializer(Provider provider) {
        this.provider = provider;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) {
        final ChannelPipeline pipeline = socketChannel.pipeline();
        pipeline.addLast(new RpcEncoder(Request.class, FastJsonSerializer.getInstance()));
        pipeline.addLast(new RpcDecoder(Response.class, FastJsonSerializer.getInstance()));
        pipeline.addLast(new ClientHandler(provider));
    }
}
