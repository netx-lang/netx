package pascal.hardscript.compiler.analysis;

import pascal.hardscript.compiler.model.scope.ComponentScope;
import pascal.hardscript.compiler.model.scope.DesignScope;

import java.util.function.Predicate;

public class GlobalAliasAnalysis extends ModelAnalysis {
    private final AliasManager manager = new AliasManager();

    public GlobalAliasAnalysis(DesignScope design) {
        super(design);
    }

    @Override
    public void analyze() {
        design.forEachSubscope(scope -> manager.addAlias(scope.getAliases()));
        manager.resolveRepresentatives();
        design.forEachSubscope(scope -> {
            scope.getEntities().removeIf(Predicate.not(manager::needDump));
            if (scope instanceof ComponentScope component) {
                component.getInputs().replaceAll(manager::repOf);
                component.getOutputs().replaceAll(manager::repOf);
            }
        });
    }
}
