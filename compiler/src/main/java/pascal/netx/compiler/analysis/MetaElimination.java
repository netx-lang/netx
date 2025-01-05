package pascal.netx.compiler.analysis;

import pascal.netx.compiler.model.instance.Instance;
import pascal.netx.compiler.model.scope.PackageScope;

import java.util.function.Predicate;

public final class MetaElimination extends ModelAnalysis {
    public MetaElimination(PackageScope design) {
        super(design);
    }

    @Override
    public void analyze() {
        pack.forEachSubComponent(scope ->
                scope.getBinding().releaseIf(Predicate.not(Instance.class::isInstance)));
    }
}
