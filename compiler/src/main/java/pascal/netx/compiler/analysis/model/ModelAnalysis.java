package pascal.netx.compiler.analysis.model;

import pascal.netx.compiler.analysis.Analysis;
import pascal.netx.compiler.circuit.scope.PackageScope;

/**
 * Base class for analyses that operate on compiler intermediate representations called models.
 */
public abstract class ModelAnalysis implements Analysis {
    protected final PackageScope pack;

    public ModelAnalysis(PackageScope pack) {
        this.pack = pack;
    }
}
