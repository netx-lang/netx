package pascal.hardscript.compiler.model;

public final class Wildcard implements Entity {
    private static final Wildcard INSTANCE = new Wildcard();

    public static Wildcard getInstance() {
        return INSTANCE;
    }
}
