package xmg.codec.exception;

public class RPcRemoteAccessException extends RuntimeException {
    private Throwable target;

    public RPcRemoteAccessException(String message, Throwable target) {
        super(message);
        this.target = target;
    }

    @Override
    public String toString() {
        return "RPcRemoteAccessException{" +
                "target=" + target.toString() +
                '}';
    }

    public void setTarget(Throwable target) {
        this.target = target;
    }

    public Throwable getTarget() {
        return target;
    }

}
