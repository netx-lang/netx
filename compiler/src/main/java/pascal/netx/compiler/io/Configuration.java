package pascal.netx.compiler.io;

import pascal.netx.compiler.model.scope.binding.ArgBindList;

public record Configuration(boolean fullyExpand, String topName, ArgBindList arguments) {

    public static Configuration of(boolean fullyExpand, String topName) {
        return new Configuration(fullyExpand, topName, null);
    }

    public static Configuration forCompact() {
        return Configuration.of(false, "top");
    }

    public static Configuration forExpanded() {
        return Configuration.of(true, "top");
    }

    public Configuration forAnotherTop(String topName) {
        return new Configuration(fullyExpand, topName, arguments);
    }

    public Configuration withBindings(ArgBindList arguments) {
        return new Configuration(fullyExpand, topName, arguments);
    }
}
