package xmg.codec;


import com.alibaba.fastjson.JSON;

import java.util.List;

public class Response {
    private final String requestId;

    private Object result;
    private Exception exception;
    private State state;
    private String address;
    private long endTime;

    private Request request;
    private List<Response> childResponse;

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
