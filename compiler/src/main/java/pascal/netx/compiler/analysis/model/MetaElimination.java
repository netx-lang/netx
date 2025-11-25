package pascal.netx.compiler.analysis.model;

import pascal.netx.compiler.circuit.scope.PackageScope;
import pascal.netx.compiler.circuit.variable.Composition;
import pascal.netx.compiler.circuit.variable.Variable;

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
                scope.getBinding().releaseIf(Predicate.not(Variable.class::isInstance)));
        pack.forEachSubComponent(scope ->
                scope.getBinding().releaseIf(Composition.class::isInstance));
    }
}
