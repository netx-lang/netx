package pascal.hardscript.util.exception;

public class ShallNotReachHereError extends AssertionError {
    public ShallNotReachHereError() {
        super("Shall not reach here");
    }
}
