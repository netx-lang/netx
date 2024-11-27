package pascal.hardscript.compiler.analysis;

import pascal.hardscript.compiler.model.scope.GlobalScope;

import java.util.function.Predicate;

public class GlobalAliasAnalysis extends ModelAnalysis {
    private final AliasManager manager = new AliasManager();

    public GlobalAliasAnalysis(GlobalScope design) {
        super(design);
    }

    @Override
    public void analyze() {
        design.forEachSubScope(scope -> manager.addAlias(scope.getAliases()));
        manager.resolveRepresentatives();
        design.forEachSubScope(scope -> {
            scope.getEntities().removeIf(Predicate.not(manager::needDump));
            scope.getInputs().replaceAll(manager::repOf);
            scope.getOutputs().replaceAll(manager::repOf);
        });
    }
}
