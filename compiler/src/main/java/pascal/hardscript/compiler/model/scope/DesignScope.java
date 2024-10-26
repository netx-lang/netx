package pascal.hardscript.compiler.model.scope;

public class DesignScope extends Scope {
    DesignScope(Scope parent) {
        super(parent);
    }

    public static DesignScope newDesign() {
        return new DesignScope(null);
    }
}
