package pascal.netx.compiler.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pascal.netx.compiler.exception.CompileCancellationException;
import pascal.netx.compiler.io.NXCompiler;
import pascal.netx.compiler.model.scope.ComponentScope;
import pascal.netx.compiler.model.scope.PackageScope;
import pascal.netx.compiler.model.scope.RepoScope;
import pascal.netx.compiler.model.variable.Access;
import pascal.netx.util.lang.BuiltinComponent;
import pascal.netx.util.lang.SourceLoc;

import java.util.List;

public final class Utils {
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

    public static RepoScope repoOf(PackageScope pack) {
        return pack.getProject();
    }

    public static RepoScope repoOf(ComponentScope component) {
        return repoOf(component.getPackage());
    }

    public static NXCompiler compilerOf(ComponentScope component) {
        return compilerOf(component.getPackage());
    }

    public static NXCompiler compilerOf(PackageScope pack) {
        return pack.getProject().getCompiler();
    }

    public static boolean isIdenticalComponent(ComponentScope component) {
        if (BuiltinComponent.of(component.getIdentifier()) == BuiltinComponent.SELECT) {
            var input = component.getInputs().getFirst();
            var output = component.getOutputs().getFirst();
            return output == input || output instanceof Access acc && acc.base() == input;
        }
        return false;
    }
}
