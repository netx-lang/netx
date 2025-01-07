package pascal.netx.compiler.io;

import pascal.netx.compiler.model.Entity;

import java.util.List;

public record Configuration(boolean fullyExpand, String topName, List<Entity> parameters) {

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
        return new Configuration(fullyExpand, topName, parameters);
    }

    public Configuration withBindings(List<Entity> parameters) {
        return new Configuration(fullyExpand, topName, parameters);
    }
}
