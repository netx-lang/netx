package pascal.netx.util.exception;

public class TodoException extends RuntimeException {
    public TodoException(String message) {
        super("TODO: " + message);
    }

    public TodoException() {
        super("TODO");
    }
}
