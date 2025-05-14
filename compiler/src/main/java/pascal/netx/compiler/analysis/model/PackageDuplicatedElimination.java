package pascal.netx.compiler.analysis.model;

import pascal.netx.compiler.analysis.model.constraint.DuplicatedManager;
import pascal.netx.compiler.model.scope.ComponentScope;
import pascal.netx.compiler.model.scope.PackageScope;

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
