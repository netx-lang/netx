package pascal.netx.compiler.analysis.model;

import pascal.netx.compiler.model.scope.ComponentScope;
import pascal.netx.compiler.model.scope.PackageScope;

/**
 * This analysis eliminates duplicated components within a package.
 * It identifies components that are structurally identical and replaces
 * them with a single representative instance, reducing redundancy in the model.
 * <p>
 * Used for expanded dump of the design, where component instantiations are not expanded
 * and duplication introduced by them is considered.
 */
public final class PackageDuplicatedElimination extends ModelAnalysis {
    private final DuplicatedManager manager = new DuplicatedManager();

    public PackageDuplicatedElimination(PackageScope design) {
        super(design);
    }

    @Override
    public void analyze() {
        pack.forEachSubComponent(manager::collectDuplicatedComponents);
        manager.resolveRepresentatives();
        pack.forEachSubComponent(this::replaceDuplicatedInstances);
    }

    private void replaceDuplicatedInstances(ComponentScope scope) {
        scope.getBinding().releaseIf(manager::needRemove);
        scope.getInputs().replaceAll(manager::repOf);
        scope.getOutputs().replaceAll(manager::repOf);
        if (scope.hasAttributes()) {
            scope.getAttributes().replaceAllVariable(manager::repOf);
        }
    }
}
