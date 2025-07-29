package pascal.netx.util.exception;

public class ShallNotReachHereError extends AssertionError {
    public ShallNotReachHereError() {
        super("Internal error: This code should not be reachable. Please report this issue.");
    }

    public ShallNotReachHereError(String message) {
        super(message);
    }
}
