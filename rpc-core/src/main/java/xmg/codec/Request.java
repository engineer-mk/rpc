package xmg.codec;


import java.lang.reflect.Method;
import java.util.UUID;

public class Request {
    private final String requestId;
    private final String methodName;
    private final Class<?>[] parameterTypes;
    private final Object[] parameters;
    private final long createTime;

    private String parentRequestId;
    private String address;
    private boolean trace = false;


    public Request(Method method, Object[] args) {
        this.requestId = UUID.randomUUID().toString();
        this.methodName = method.getName();
        this.parameterTypes = method.getParameterTypes();
        this.parameters = args;
        this.createTime = System.currentTimeMillis();
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
