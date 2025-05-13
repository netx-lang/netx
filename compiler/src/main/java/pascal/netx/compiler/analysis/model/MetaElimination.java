package pascal.netx.compiler.analysis.model;

import pascal.netx.compiler.model.scope.PackageScope;
import pascal.netx.compiler.model.variable.Composition;
import pascal.netx.compiler.model.variable.Variable;

import java.util.function.Predicate;

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
