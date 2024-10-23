package pascal.hardscript.exception;

public class TodoException extends RuntimeException {
    public TodoException(String message) {
        super("TODO:" + message);
    }
}
