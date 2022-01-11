package xmg.codec;


import xmg.client.RpcClient;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.UUID;

public class Request implements Serializable {
    private static final long serialVersionUID = 3425084385L;
    private String token;
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
        this.token = RpcClient.TOKEN;
        this.methodName = method.getName();
        this.parameterTypes = method.getParameterTypes();
        this.parameters = args;
        this.createTime = System.currentTimeMillis();
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
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
