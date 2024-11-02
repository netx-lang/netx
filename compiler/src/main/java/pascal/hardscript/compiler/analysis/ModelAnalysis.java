package pascal.hardscript.compiler.analysis;

import pascal.hardscript.compiler.model.scope.DesignScope;

public abstract class ModelAnalysis implements Analysis {
    protected final DesignScope design;

    public ModelAnalysis(DesignScope design) {
        this.design = design;
    }
}
