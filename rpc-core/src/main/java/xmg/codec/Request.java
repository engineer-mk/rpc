package xmg.codec;


import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.UUID;

public class Request {
    private String requestId;
    private String methodName;
    private Class<?>[] parameterTypes;
    private Object[] parameters;

    public Request() {
    }

    public Request(Method method, Object[] args) {
        this.requestId = UUID.randomUUID().toString();
        this.methodName = method.getName();
        this.parameterTypes = method.getParameterTypes();
        this.parameters = args;
    }

    @Override
    public String toString() {
        return "Request{" +
                "requestId='" + requestId + '\'' +
                ", methodName='" + methodName + '\'' +
                ", parameterTypes=" + Arrays.toString(parameterTypes) +
                ", parameters=" + Arrays.toString(parameters) +
                '}';
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(Class<?>[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public Object[] getParameters() {
        return parameters;
    }

    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }
}
