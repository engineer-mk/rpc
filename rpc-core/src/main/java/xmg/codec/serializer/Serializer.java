package xmg.codec.serializer;

public interface Serializer {

    byte[] serialize(Object obj);

    Object deserialize(byte[] bytes, Class<?> clazz);
}
