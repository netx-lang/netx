package pascal.netx.compiler.model.scope;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pascal.netx.compiler.exception.CompileCancellationException;
import pascal.netx.compiler.io.PackageManager;

import java.util.function.Consumer;
import java.util.stream.Stream;

public final class ProjectScope extends Scope {
    private static final Logger log = LoggerFactory.getLogger(ProjectScope.class);

    private final Info info;
    private final PackageManager manager;

    private PackageScope top;

    public ProjectScope(Info info, PackageManager manager) {
        super(null);
        this.info = info;
        this.manager = manager;
    }

    public PackageScope getTop() {
        return top;
    }

    public void setTop(PackageScope pack) {
        if (top != null) {
            log.error("Cannot conclude the application package of the project");
            throw new CompileCancellationException();
        }
        this.top = pack;
    }

    public void forEachPackage(Consumer<PackageScope> consumer) {
        getChildren().forEach(child -> consumer.accept((PackageScope) child));
    }

    public Stream<PackageScope> packages() {
        return getChildren().stream().map(PackageScope.class::cast);
    }

    public Info getInfo() {
        return info;
    }

    public record Info(String name, Version version) {
        private static final Info EMPTY = new Info(null, null);

        public static Info empty() {
            return EMPTY;
        }

        public boolean isEmpty() {
            return this == EMPTY;
        }
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
