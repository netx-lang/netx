package pascal.netx.compiler.io;

import java.util.List;

public final class PackageManager {
    private final List<NXCompiler> dependencies;
    private final ImportManager manager;

    public PackageManager(List<NXCompiler> dependencies, ImportManager manager) {
        this.dependencies = dependencies;
        this.manager = manager;
    }

    public static PackageManager ofEmpty() {
        return new PackageManager(List.of(), ImportManager.ofEmpty());
    }
}
