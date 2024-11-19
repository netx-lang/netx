package pascal.hardscript.compiler.analysis;

import pascal.hardscript.compiler.analysis.constraint.DuplicatedManager;
import pascal.hardscript.compiler.model.scope.DesignScope;
import pascal.hardscript.compiler.model.scope.Scope;

public final class LocalDuplicatedElimination extends LocalModelAnalysis {

    public LocalDuplicatedElimination(DesignScope design) {
        super(design, scope -> new ComponentDuplicatedAnalysis(scope).analyze(scope));
    }

    private static class ComponentDuplicatedAnalysis extends ComponentAnalysis {
        private final DuplicatedManager manager = new DuplicatedManager();

        protected ComponentDuplicatedAnalysis(Scope scope) {
            super(scope);
        }

        public void analyze(Scope scope) {
            exploreAnalysisScopes(scope);
            inlineScope.forEach(manager::collectDuplicatedComponents);
            eliminateDuplicatedData(manager::needRemove, manager::repOf);
        }
    }
}
