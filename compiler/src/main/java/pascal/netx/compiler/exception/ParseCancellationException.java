package pascal.netx.compiler.exception;

public class ParseCancellationException extends RuntimeException {
    public ParseCancellationException() {
        super("Parsing was cancelled due to an error.");
    }
}
