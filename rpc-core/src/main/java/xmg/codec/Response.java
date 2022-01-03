package xmg.codec;


import com.alibaba.fastjson.JSON;

import java.util.List;

public class Response {
    private String requestId;

    private Object result;
    private Exception exception;
    private State state;
    private String address;
    private long endTime;

    private Request request;
    private List<Response> childResponse;

    public String getChildResponseInfo(){
        if (childResponse == null || childResponse.size() == 0) {
            return " ";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("--->{ ");
        for (Response response : childResponse) {
            sb.append(response.getTraceInfo());
        }
        sb.append(" }--->");
        return sb.toString();
    }

    public String getTraceInfo(){
        StringBuilder sb = new StringBuilder();
        if (request != null) {
            sb.append(request.getMethodName());
            sb.append("(");
            final Object[] parameters = request.getParameters();
            final Class<?>[] parameterTypes = request.getParameterTypes();
            for (int i = 0; i < parameters.length; i++) {
                final Object parameter = parameters[i];
                final Class<?> parameterType = parameterTypes[i];
                if (i>0){
                    sb.append(",");
                }
                sb.append(parameterType.getSimpleName());
                sb.append(" ");
                sb.append(JSON.toJSONString(parameter));
            }
            sb.append(")");
        }
        sb.append("--->");
        sb.append(this.address);
        sb.append(this.getChildResponseInfo());
        sb.append("调用结果:");
        sb.append(JSON.toJSONString(result));
        return sb.toString();
    }


    public Response() {
    }

    public Response(String requestId) {
        this.requestId = requestId;
    }

    public enum State {
        OK,
        NOT_FOUND,
        INTERNAL_SERVER_ERROR,

    }

    public long getEndTime() {
        return endTime;
    }

    public Long getTimeConsuming() {
        if (request == null) {
            return null;
        }
        return this.endTime - request.getCreateTime();
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public List<Response> getChildResponse() {
        return childResponse;
    }

    public void setChildResponse(List<Response> childResponse) {
        this.childResponse = childResponse;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

    public String getRequestId() {
        return requestId;
    }


    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public State getStates() {
        return state;
    }

    public void setStates(State state) {
        this.state = state;
    }
}
