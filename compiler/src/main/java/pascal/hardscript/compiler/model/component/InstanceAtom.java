package pascal.hardscript.compiler.model.component;

import pascal.hardscript.compiler.model.instance.Instance;
import pascal.hardscript.util.lang.SourceLoc;

public final class InstanceAtom extends AtomType {
    private final Instance instance;
    private final SourceLoc sourceLoc;

    private InstanceAtom(Instance instance, SourceLoc sourceLoc) {
        this.instance = instance;
        this.sourceLoc = sourceLoc;
    }

    public static InstanceAtom of(Instance source, SourceLoc sourceLoc) {
        return new InstanceAtom(source, sourceLoc);
    }

    public Instance getInstance() {
        return instance;
    }

    public SourceLoc getSourceLoc() {
        return sourceLoc;
    }
}
