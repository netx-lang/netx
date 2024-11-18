package pascal.hardscript.compiler.analysis;

import pascal.hardscript.compiler.analysis.constraint.DuplicatedManager;
import pascal.hardscript.compiler.model.scope.ComponentScope;
import pascal.hardscript.compiler.model.scope.DesignScope;
import pascal.hardscript.compiler.model.scope.Scope;

import java.util.LinkedList;
import java.util.List;

public final class LocalDuplicatedElimination extends ModelAnalysis {

    public LocalDuplicatedElimination(DesignScope design) {
        super(design);
    }

    private static boolean isUserDefinedComponent(Scope scope) {
        return !((ComponentScope) scope).isBuiltin();
    }

    @Override
    public void analyze() {
        design.forEachScope(scope -> {
            if (isUserDefinedComponent(scope)) {
                new ComponentAnalysis().analyze(scope);
            }
        });
    }

    private static class ComponentAnalysis {
        private final DuplicatedManager manager = new DuplicatedManager();
        private final List<Scope> inlineScope = new LinkedList<>();
        private final List<Scope> instantiateScope = new LinkedList<>();

        public void analyze(Scope scope) {
            exploreAnalysisScopes(scope);
            eliminateDuplicatedData();
        }

        private void exploreAnalysisScopes(Scope scope) {
            inlineScope.add(scope);
            manager.collectDuplicatedComponents(scope);
            for (var child : scope.getChildren()) {
                if (!isUserDefinedComponent(child)) {
                    exploreAnalysisScopes(child);
                } else {
                    instantiateScope.add(child);
                }
            }
        }

        public void eliminateDuplicatedData() {
            inlineScope.forEach(scope -> {
                scope.getEntities().removeIf(manager::needRemove);
                if (scope instanceof ComponentScope componentScope) {
                    componentScope.getInputs().replaceAll(manager::repOf);
                    componentScope.getOutputs().replaceAll(manager::repOf);
                }
            });

            instantiateScope.forEach(scope -> {
                if (scope instanceof ComponentScope componentScope) {
                    componentScope.getInputs().replaceAll(manager::repOf);
                    componentScope.getOutputs().replaceAll(manager::repOf);
                }
            });
        }
    }
}
