package pascal.netx.compiler.backend.analysis.model;

import pascal.netx.compiler.model.circuit.scope.PackageScope;
import pascal.netx.compiler.model.circuit.signal.Composition;
import pascal.netx.compiler.model.circuit.signal.Signal;

import java.util.function.Predicate;

/**
 * MetaElimination is an analysis that removes meta variables from the design.
 */
public final class MetaElimination extends ModelAnalysis {
    public MetaElimination(PackageScope design) {
        super(design);
    }

    @Override
    public void analyze() {
        pack.forEachSubComponent(scope ->
                scope.getBinding().releaseIf(Predicate.not(Signal.class::isInstance)));
        pack.forEachSubComponent(scope ->
                scope.getBinding().releaseIf(Composition.class::isInstance));
    }
}
