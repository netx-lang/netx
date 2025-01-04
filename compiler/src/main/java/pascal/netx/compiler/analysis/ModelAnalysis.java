package pascal.netx.compiler.analysis;

import pascal.netx.compiler.model.scope.ModuleScope;

public abstract class ModelAnalysis implements Analysis {
    protected final ModuleScope design;

    public ModelAnalysis(ModuleScope design) {
        this.design = design;
    }
}
