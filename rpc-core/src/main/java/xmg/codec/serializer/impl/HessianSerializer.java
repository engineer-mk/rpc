package xmg.codec.serializer.impl;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xmg.codec.Response;
import xmg.codec.serializer.Serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class HessianSerializer implements Serializer {
    private static final Logger log = LoggerFactory.getLogger(HessianSerializer.class);

    private static volatile Serializer serializer;

    public static Serializer getInstance() {
        if (serializer == null) {
            synchronized (HessianSerializer.class) {
                if (serializer == null) {
                    serializer = new HessianSerializer();
                }
            }
        }
        return serializer;
    }

    private HessianSerializer() {
    }

    @Override
    public byte[] serialize(Object obj) {
        try {
            return doSerialize(obj);
        } catch (Exception e) {
            if (obj instanceof Response) {
                final Response bad = (Response) obj;
                final Response response = new Response(bad.getRequestId());
                response.setAddress(bad.getAddress());
                response.setState(bad.getState());
                response.setThrowable(e);
                return doSerialize(response);
            }
        }
        return new byte[]{};
    }

    private byte[] doSerialize(Object obj) {
        Hessian2Output ho = null;
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            ho = new Hessian2Output(os);
            ho.writeObject(obj);
            ho.flush();
            return os.toByteArray();
        } catch (Exception e) {
            final String errorMessage = "序列化异常:" + e.getLocalizedMessage();
            log.warn(errorMessage);
            throw new RuntimeException(errorMessage);
        } finally {
            if (ho != null) {
                try {
                    ho.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public Object deserialize(byte[] bytes, Class<?> clazz) {
        Hessian2Input in = null;
        try (ByteArrayInputStream is = new ByteArrayInputStream(bytes)) {
            in = new Hessian2Input(is);
            return in.readObject(clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
