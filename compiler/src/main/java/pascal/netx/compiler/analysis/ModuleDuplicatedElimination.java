package pascal.netx.compiler.analysis;

import pascal.netx.compiler.analysis.constraint.DuplicatedManager;
import pascal.netx.compiler.model.scope.ComponentScope;
import pascal.netx.compiler.model.scope.ModuleScope;

public final class ModuleDuplicatedElimination extends ModelAnalysis {
    private final DuplicatedManager manager = new DuplicatedManager();

    public ModuleDuplicatedElimination(ModuleScope design) {
        super(design);
    }

    @Override
    public void analyze() {
        design.forEachSubScope(manager::collectDuplicatedComponents);
        design.forEachSubScope(this::replaceDuplicatedInstances);
    }

    private void replaceDuplicatedInstances(ComponentScope scope) {
        scope.getBinding().releaseIf(manager::needRemove);
        scope.getInputs().replaceAll(manager::repOf);
        scope.getOutputs().replaceAll(manager::repOf);
    }
}
