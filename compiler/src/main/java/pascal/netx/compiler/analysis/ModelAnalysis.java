package pascal.netx.compiler.analysis;

import pascal.netx.compiler.model.scope.GlobalScope;

public abstract class ModelAnalysis implements Analysis {
    protected final GlobalScope design;

    public ModelAnalysis(GlobalScope design) {
        this.design = design;
    }
}
