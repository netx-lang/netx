package pascal.netx.compiler.model.scope;

import pascal.netx.compiler.elaborator.Logger;
import pascal.netx.compiler.exception.CompileCancellationException;
import pascal.netx.compiler.io.NXCompiler;
import pascal.netx.compiler.io.PackageManager;
import pascal.netx.util.exception.ShallNotReachHereError;

import java.util.List;

/**
 * A RepoScope corresponds to a NetX repository that contains an application package and multiple library packages.
 * In a RepoScope, the unique application package becomes the top-level package,
 * and the top-level component in that package becomes the entry point of the whole project.
 */
public final class RepoScope extends Scope {
    private PackageScope top;
    private NXCompiler compiler;
    private PackageManager manager;

    public RepoScope() {
        super(null);
    }

    public PackageManager getManager() {
        return manager;
    }

    public NXCompiler getCompiler() {
        return compiler;
    }

    public void setCompiler(NXCompiler compiler) {
        if (this.compiler != null) {
            throw new ShallNotReachHereError();
        }
        this.compiler = compiler;
    }

    public void setManager(PackageManager manager) {
        if (this.manager != null) {
            throw new ShallNotReachHereError();
        }
        this.manager = manager;
    }

    public PackageScope getTop() {
        return top;
    }

    public void setTop(PackageScope pack) {
        if (top != null) {
            Logger.error("Cannot conclude the application package of the project");
            throw new CompileCancellationException();
        }
        this.top = pack;
    }

    public List<PackageScope> packages() {
        return getChildren().stream().map(PackageScope.class::cast).toList();
    }

    public record Info(String name, Version version) {
    }

    public record Version(int major, int minor, int patch) implements Comparable<Version> {
        public static Version of(String version) {
            var parts = version.split("\\.");
            return new Version(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
        }

        @Override
        public int compareTo(Version other) {
            if (major != other.major) {
                return Integer.compare(major, other.major);
            }
            if (minor != other.minor) {
                return Integer.compare(minor, other.minor);
            }
            return Integer.compare(patch, other.patch);
        }

    }
}
