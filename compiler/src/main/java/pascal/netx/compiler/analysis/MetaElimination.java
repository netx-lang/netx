package pascal.netx.compiler.analysis;

import pascal.netx.compiler.model.instance.Instance;
import pascal.netx.compiler.model.scope.ModuleScope;

import java.util.function.Predicate;

public final class MetaElimination extends ModelAnalysis {
    public MetaElimination(ModuleScope design) {
        super(design);
    }

    @Override
    public void analyze() {
        design.forEachSubScope(scope ->
                scope.getBinding().releaseIf(Predicate.not(Instance.class::isInstance)));
    }
}
