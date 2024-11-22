package pascal.hardscript.compiler.model.scope;

public final class BlockScope extends Scope {
    BlockScope(DesignScope global, Scope parent) {
        super(global, parent);
    }
}
