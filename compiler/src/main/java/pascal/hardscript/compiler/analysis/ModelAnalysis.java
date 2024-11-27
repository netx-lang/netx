package pascal.hardscript.compiler.analysis;

import pascal.hardscript.compiler.model.scope.GlobalScope;

public abstract class ModelAnalysis implements Analysis {
    protected final GlobalScope design;

    public ModelAnalysis(GlobalScope design) {
        this.design = design;
    }
}
