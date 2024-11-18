package pascal.hardscript.compiler.analysis;

import pascal.hardscript.compiler.model.scope.ComponentScope;
import pascal.hardscript.compiler.model.scope.DesignScope;

import java.util.function.Predicate;

public class GlobalAliasAnalysis extends ModelAnalysis {
    private final AliasStructure global = new AliasStructure();

    public GlobalAliasAnalysis(DesignScope design) {
        super(design);
    }

    @Override
    public void analyze() {
        design.forEachScope(scope -> global.addAlias(scope.getAliases()));
        global.resolveRepresentatives();
        design.forEachScope(scope -> {
            scope.getEntities().removeIf(Predicate.not(global::needDump));
            if (scope instanceof ComponentScope component) {
                component.getInputs().replaceAll(global::repOf);
                component.getOutputs().replaceAll(global::repOf);
            }
        });
    }
}
