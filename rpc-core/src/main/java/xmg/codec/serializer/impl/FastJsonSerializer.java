package xmg.codec.serializer.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import xmg.codec.serializer.Serializer;


public class FastJsonSerializer implements Serializer {
    private static volatile Serializer serializer;

    public static Serializer getInstance() {
        if (serializer == null) {
            synchronized (FastJsonSerializer.class) {
                if (serializer == null) {
                    serializer = new FastJsonSerializer();
                }
            }
        }
        return serializer;
    }

    static {
        ParserConfig.getGlobalInstance().addAccept("com.xmg.");
    }

    private FastJsonSerializer() {
    }

    @Override
    public byte[] serialize(Object obj) {
        return JSON.toJSONBytes(obj, SerializerFeature.WriteClassName);
    }

    @Override
    public Object deserialize(byte[] bytes, Class<?> clazz) {
        return JSON.parseObject(bytes, clazz);
    }
}
