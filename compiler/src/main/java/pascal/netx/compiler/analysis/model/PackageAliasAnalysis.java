package pascal.netx.compiler.analysis.model;

import pascal.netx.compiler.model.scope.PackageScope;

import java.util.function.Predicate;

/**
 * Analyzes alias relations in a package, propagating the intra-component alias relations to other components.
 * This is used for expanded dump of the design, where component instantiations are expanded.
 */
public final class PackageAliasAnalysis extends ModelAnalysis {
    private final AliasManager manager = new AliasManager();

    public PackageAliasAnalysis(PackageScope design) {
        super(design);
    }

    @Override
    public void analyze() {
        pack.forEachSubComponent(manager::addAliasForScope);
        manager.resolveRepresentatives();
        pack.forEachSubComponent(scope -> {
            scope.getBinding().releaseIf(Predicate.not(manager::needDump));
            scope.getInputs().replaceAll(manager::repOf);
            scope.getOutputs().replaceAll(manager::repOf);
            if (scope.hasAttributes()) {
                scope.getAttributes().replaceAllVariable(manager::repOf);
            }
            manager.clearIn(scope);
        });
    }
}
