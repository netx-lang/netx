package pascal.netx.util.exception;

public class ShallNotReachHereError extends AssertionError {
    public ShallNotReachHereError() {
        super("Shall not reach here");
    }

    public ShallNotReachHereError(String message) {
        super(message);
    }
}
