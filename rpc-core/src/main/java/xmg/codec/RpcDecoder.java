package xmg.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import xmg.codec.serializer.Serializer;

import java.util.List;


public class RpcDecoder extends ReplayingDecoder<Void> {

    private final Class<?> aClass;
    private final Serializer serializer;

    public RpcDecoder(Class<?> aClass, Serializer serializer) {
        this.aClass = aClass;
        this.serializer = serializer;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> list) throws Exception {
        int length = in.readInt();
        byte[] data = new byte[length];
        in.readBytes(data);
        final Object object = serializer.deserialize(data, aClass);
        list.add(object);
    }
}
