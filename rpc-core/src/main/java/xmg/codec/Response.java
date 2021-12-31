package xmg.codec;


public class Response {
    private String requestId;
    private Object result;
    private Exception exception;
    private States states;

    public enum States {
        OK(200, "OK"),
        NOT_FOUND(404, "Not Found"),
        INTERNAL_SERVER_ERROR(500, "Internal Server Error");
        private final int value;

        private final String reasonPhrase;

        States(int value, String reasonPhrase) {
            this.value = value;
            this.reasonPhrase = reasonPhrase;
        }
    }

    @Override
    public String toString() {
        return "Response{" +
                "requestId='" + requestId + '\'' +
                ", result=" + result +
                ", exception=" + exception +
                ", states=" + states +
                '}';
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
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

    public States getStates() {
        return states;
    }

    public void setStates(States states) {
        this.states = states;
    }
}
