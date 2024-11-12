package pascal.hardscript.compiler.analysis;

import pascal.hardscript.compiler.model.scope.Design;

public abstract class ModelAnalysis implements Analysis {
    protected final Design design;

    public ModelAnalysis(Design design) {
        this.design = design;
    }
}
