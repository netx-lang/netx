package pascal.netx.compiler.backend.analysis.circuit;

import pascal.netx.compiler.model.circuit.scope.PackageScope;
import pascal.netx.compiler.model.circuit.signal.Composition;
import pascal.netx.compiler.model.circuit.signal.Signal;

import java.util.function.Predicate;

/**
 * MetaElimination is an analysis that removes meta variables from the design.
 */
public final class MetaElimination extends CircuitAnalysis {
    public MetaElimination(PackageScope design) {
        super(design);
    }

    @Override
    public void analyze() {
        pack.forEachSubComponent(scope ->
                scope.releaseEntityIf(Predicate.not(Signal.class::isInstance)));
        pack.forEachSubComponent(scope ->
                scope.releaseEntityIf(Composition.class::isInstance));
    }
}
