package pascal.hardscript.compiler.analysis;

import pascal.hardscript.compiler.analysis.constraint.DuplicatedManager;
import pascal.hardscript.compiler.model.scope.ComponentScope;
import pascal.hardscript.compiler.model.scope.GlobalScope;

public final class GlobalDuplicatedElimination extends ModelAnalysis {
    private final DuplicatedManager manager = new DuplicatedManager();

    public GlobalDuplicatedElimination(GlobalScope design) {
        super(design);
    }

    @Override
    public void analyze() {
        design.forEachSubScope(manager::collectDuplicatedComponents);
        design.forEachSubScope(this::replaceDuplicatedInstances);
    }

    private void replaceDuplicatedInstances(ComponentScope scope) {
        scope.getEntities().removeIf(manager::needRemove);
        scope.getInputs().replaceAll(manager::repOf);
        scope.getOutputs().replaceAll(manager::repOf);
    }
}
