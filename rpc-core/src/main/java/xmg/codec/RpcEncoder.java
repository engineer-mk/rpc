package xmg.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import xmg.codec.serializer.Serializer;


public class RpcEncoder extends MessageToByteEncoder<Object> {

    private final Class<?> aClass;

    private final Serializer serializer;

    public RpcEncoder(Class<?> aClass, Serializer serializer) {
        this.aClass = aClass;
        this.serializer = serializer;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object obj, ByteBuf out) {
        if (aClass.isInstance(obj)) {
            final byte[] bytes = serializer.serialize(obj);
            out.writeInt(bytes.length);
            out.writeBytes(bytes);
        }
    }

}
