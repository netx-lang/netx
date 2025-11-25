package pascal.netx.compiler.utils;

public class ParseCancellationException extends RuntimeException {
    public ParseCancellationException() {
        super("Parsing was cancelled due to an error.");
    }
}
