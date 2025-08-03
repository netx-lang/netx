package pascal.netx.compiler.model;

import pascal.netx.compiler.elaborator.Logger;
import pascal.netx.compiler.exception.CompileCancellationException;
import pascal.netx.compiler.io.NXCompiler;
import pascal.netx.compiler.model.scope.ComponentScope;
import pascal.netx.compiler.model.scope.PackageScope;
import pascal.netx.compiler.model.scope.RepoScope;
import pascal.netx.util.lang.SourceLoc;

import java.util.List;

public final class Utils {
    public static int requiresMatchedSize(SourceLoc sourceLoc, List<?> target, List<?> source) {
        if (target.size() != source.size()) {
            Logger.error(sourceLoc, "Mismatched component size: " + target.size() + " vs " + source.size());
            throw new CompileCancellationException();
        }
        return target.size();
    }

    public static void requiresMatchedSize(SourceLoc sourceLoc, int expected, List<?> actual) {
        if (expected != actual.size()) {
            Logger.error(sourceLoc, "Expected list of size {} but got {}", expected, actual.size());
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

}
