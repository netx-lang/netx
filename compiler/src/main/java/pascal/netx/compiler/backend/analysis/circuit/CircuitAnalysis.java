package pascal.netx.compiler.backend.analysis.circuit;

import pascal.netx.compiler.backend.analysis.Analysis;
import pascal.netx.compiler.model.circuit.scope.PackageScope;

/**
 * Base class for analyses that operate on compiler intermediate representations called models.
 */
public abstract class CircuitAnalysis implements Analysis {
    protected final PackageScope pack;

    public CircuitAnalysis(PackageScope pack) {
        this.pack = pack;
    }
}
