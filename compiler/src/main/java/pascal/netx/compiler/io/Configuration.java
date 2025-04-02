package pascal.netx.compiler.io;

import pascal.netx.compiler.model.scope.binding.ArgBindList;

public record Configuration(boolean fullyExpand, String topName, ArgBindList arguments) {

    public static Configuration forCompact() {
        return new Configuration(false, "top", null);
    }

    public static Configuration forExpanded() {
        return new Configuration(true, "top", null);
    }

    public Configuration withTop(String topName) {
        return new Configuration(fullyExpand, topName, arguments);
    }

    public Configuration withArgs(ArgBindList arguments) {
        return new Configuration(fullyExpand, topName, arguments);
    }
}
