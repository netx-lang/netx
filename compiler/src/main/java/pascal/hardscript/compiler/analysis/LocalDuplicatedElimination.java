package pascal.hardscript.compiler.analysis;

import pascal.hardscript.compiler.analysis.constraint.DuplicatedManager;
import pascal.hardscript.compiler.model.scope.ComponentScope;
import pascal.hardscript.compiler.model.scope.GlobalScope;

public final class LocalDuplicatedElimination extends LocalModelAnalysis {

    public LocalDuplicatedElimination(GlobalScope design) {
        super(design, scope -> new ComponentDuplicatedAnalysis(scope).analyze(scope));
    }

    private static class ComponentDuplicatedAnalysis extends ComponentAnalysis {
        private final DuplicatedManager manager = new DuplicatedManager();

        protected ComponentDuplicatedAnalysis(ComponentScope scope) {
            super(scope);
        }

        public void analyze(ComponentScope scope) {
            exploreAnalysisScopes(scope);
            anaScope.forEach(manager::collectDuplicatedComponents);
            eliminateDuplicatedData(manager::needRemove, manager::repOf);
        }
    }
}
