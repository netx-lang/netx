package pascal.hardscript.compiler.analysis;

import pascal.hardscript.compiler.model.instance.Instance;
import pascal.hardscript.compiler.model.scope.DesignScope;

import java.util.function.Predicate;

public class MetaElimination extends ModelAnalysis {
    public MetaElimination(DesignScope design) {
        super(design);
    }

    @Override
    public void analyze() {
        design.forEachSubScope(scope -> scope.getEntities().removeIf(Predicate.not(Instance.class::isInstance)));
    }
}
