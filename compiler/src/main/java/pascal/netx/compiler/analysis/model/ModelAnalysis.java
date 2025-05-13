package pascal.netx.compiler.analysis.model;

import pascal.netx.compiler.analysis.Analysis;
import pascal.netx.compiler.model.scope.PackageScope;

public abstract class ModelAnalysis implements Analysis {
    protected final PackageScope pack;

    public ModelAnalysis(PackageScope pack) {
        this.pack = pack;
    }
}
