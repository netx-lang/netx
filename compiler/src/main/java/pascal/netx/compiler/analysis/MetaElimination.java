package pascal.netx.compiler.analysis;

import pascal.netx.compiler.model.instance.Instance;
import pascal.netx.compiler.model.scope.GlobalScope;

import java.util.function.Predicate;

public final class MetaElimination extends ModelAnalysis {
    public MetaElimination(GlobalScope design) {
        super(design);
    }

    @Override
    public void analyze() {
        design.forEachSubScope(scope ->
                scope.getEntities().removeIf(Predicate.not(Instance.class::isInstance)));
    }
}
