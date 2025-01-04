package pascal.netx.compiler.analysis;

import pascal.netx.compiler.model.scope.ModuleScope;

import java.util.function.Predicate;

public final class ModuleAliasAnalysis extends ModelAnalysis {
    private final AliasManager manager = new AliasManager();

    public ModuleAliasAnalysis(ModuleScope design) {
        super(design);
    }

    @Override
    public void analyze() {
        design.forEachSubScope(scope -> manager.addAlias(scope.getAliases()));
        manager.resolveRepresentatives();
        design.forEachSubScope(scope -> {
            scope.getBinding().releaseIf(Predicate.not(manager::needDump));
            scope.getInputs().replaceAll(manager::repOf);
            scope.getOutputs().replaceAll(manager::repOf);
        });
    }
}
