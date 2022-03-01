package xmg.codec;


import java.io.Serializable;
import java.util.List;

public class Response implements Serializable {
    private static final long serialVersionUID = 153214121232L;

    private String requestId;
    private Object result;
    private Throwable throwable;
    private State state;
    private String address;
    private long endTime;

    private Request request;
    private List<Response> childResponse;

    public String getChildResponseInfo() {
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

    public String getTraceInfo() {
        StringBuilder sb = new StringBuilder();
        if (request != null) {
            sb.append(request.getMethodName());
            sb.append("(");
            final Object[] parameters = request.getParameters();
            final Class<?>[] parameterTypes = request.getParameterTypes();
            if (parameters != null) {
                for (int i = 0; i < parameters.length; i++) {
                    final Object parameter = parameters[i];
                    final Class<?> parameterType = parameterTypes[i];
                    if (i > 0) {
                        sb.append(",");
                    }
                    sb.append(parameterType.getSimpleName());
                    sb.append(" ");
                    sb.append(parameter.toString());
                }
            }
            sb.append(")");
        }
        sb.append("--->");
        sb.append(this.address);
        sb.append(this.getChildResponseInfo());
        if (!this.state.equals(State.OK)) {
            Throwable throwable = this.throwable;
            sb.append("调用异常:");
            sb.append(throwable.toString());
        } else if (result != null) {
            sb.append("调用结果:");
            sb.append(result.toString());
        }
        if (this.request != null) {
            sb.append(",调用耗时:");
            sb.append(this.getTimeConsuming());
            sb.append("ms ");
        }
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


    public String getRequestId() {
        return requestId;
    }


    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    public State getStates() {
        return state;
    }

    public void setStates(State state) {
        this.state = state;
    }
}
