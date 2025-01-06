package pascal.netx.compiler.model.expr;

import pascal.netx.compiler.model.instance.Instance;
import pascal.netx.util.lang.SourceLoc;

public final class InstanceAtom extends AtomExpr {
    private final Mode mode;
    private final Instance instance;
    private final SourceLoc sourceLoc;

    private InstanceAtom(Mode mode, Instance instance, SourceLoc sourceLoc) {
        this.mode = mode;
        this.instance = instance;
        this.sourceLoc = sourceLoc;
    }

    public static InstanceAtom of(Mode mode, Instance source, SourceLoc sourceLoc) {
        return new InstanceAtom(mode, source, sourceLoc);
    }

    public Mode getMode() {
        return mode;
    }

    public Instance getInstance() {
        return instance;
    }

    public SourceLoc getSourceLoc() {
        return sourceLoc;
    }

    public enum Mode {
        SOURCE, SINK, BIDI
    }
}
