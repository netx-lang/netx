package pascal.netx.compiler.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pascal.netx.compiler.exception.CompileCancellationException;
import pascal.netx.util.lang.SourceLoc;

import java.util.List;

public class Utils {
    private static final Logger log = LoggerFactory.getLogger(Utils.class);

    public static int requiresMatchedSize(SourceLoc sourceLoc, List<?> target, List<?> source) {
        if (target.size() != source.size()) {
            log.error("{}: Instantiation with mismatched component size: {} vs {}",
                    sourceLoc, target.size(), source.size());
            throw new CompileCancellationException();
        }
        return target.size();
    }

    public static void requiresMatchedSize(SourceLoc sourceLoc, int expected, List<?> actual) {
        if (expected != actual.size()) {
            log.error("{}: Expected list of size {} but got {}", sourceLoc, expected, actual);
            throw new CompileCancellationException();
        }
    }
}
