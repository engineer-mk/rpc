package xmg.codec;


import com.caucho.hessian.io.Hessian2Output;
import xmg.codec.serializer.Serializer;
import xmg.codec.serializer.impl.HessianSerializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.UUID;

public class Request implements Serializable {
    private static final long serialVersionUID = 3425084385L;

    private String requestId;
    private String methodName;
    private Class<?>[] parameterTypes;
    private Object[] parameters;
    private long createTime;

    private String parentRequestId;
    private String address;
    private boolean trace = false;

    public Request() {
    }

    public Request(Method method, Object[] args) {
        this.requestId = UUID.randomUUID().toString();
        this.methodName = method.getName();
        this.parameterTypes = method.getParameterTypes();
        this.parameters = args;
        this.createTime = System.currentTimeMillis();
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public void setParameterTypes(Class<?>[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getCreateTime() {
        return createTime;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isTrace() {
        return trace;
    }

    public void setTrace(boolean trace) {
        this.trace = trace;
    }

    public String getParentRequestId() {
        return parentRequestId;
    }

    public void setParentRequestId(String parentRequestId) {
        this.parentRequestId = parentRequestId;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getMethodName() {
        return methodName;
    }

    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    public Object[] getParameters() {
        return parameters;
    }
}
