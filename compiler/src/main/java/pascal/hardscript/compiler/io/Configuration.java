package pascal.hardscript.compiler.io;

public record Configuration(boolean fullyExpand, String topName) {

    public static Configuration of(boolean fullyExpand, String topName) {
        return new Configuration(fullyExpand, topName);
    }

    public static Configuration forCompact() {
        return new Configuration(false, "top");
    }

    public static Configuration forExpanded() {
        return new Configuration(true, "top");
    }
}
