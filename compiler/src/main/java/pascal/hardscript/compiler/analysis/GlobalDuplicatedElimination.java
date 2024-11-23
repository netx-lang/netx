package pascal.hardscript.compiler.analysis;

import pascal.hardscript.compiler.analysis.constraint.DuplicatedManager;
import pascal.hardscript.compiler.model.scope.ComponentScope;
import pascal.hardscript.compiler.model.scope.DesignScope;
import pascal.hardscript.compiler.model.scope.Scope;

public final class GlobalDuplicatedElimination extends ModelAnalysis {
    private final DuplicatedManager manager = new DuplicatedManager();

    public GlobalDuplicatedElimination(DesignScope design) {
        super(design);
    }

    @Override
    public void analyze() {
        design.forEachSubscope(manager::collectDuplicatedComponents);
        design.forEachSubscope(this::replaceDuplicatedInstances);
    }

    private void replaceDuplicatedInstances(Scope scope) {
        scope.getEntities().removeIf(manager::needRemove);
        if (scope instanceof ComponentScope componentScope) {
            componentScope.getInputs().replaceAll(manager::repOf);
            componentScope.getOutputs().replaceAll(manager::repOf);
        }
    }
}
