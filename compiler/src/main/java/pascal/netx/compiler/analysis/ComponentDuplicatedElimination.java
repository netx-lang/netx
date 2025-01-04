package pascal.netx.compiler.analysis;

import pascal.netx.compiler.analysis.constraint.DuplicatedManager;
import pascal.netx.compiler.model.scope.ComponentScope;
import pascal.netx.compiler.model.scope.ModuleScope;

public final class ComponentDuplicatedElimination extends ComponentModelAnalysis {

    public ComponentDuplicatedElimination(ModuleScope design) {
        super(design, scope -> new Analyzer(scope).analyze(scope));
    }

    private static class Analyzer extends ComponentModelAnalysis.Analyzer {
        private final DuplicatedManager manager = new DuplicatedManager();

        protected Analyzer(ComponentScope scope) {
            super(scope);
        }

        public void analyze(ComponentScope scope) {
            exploreAnalysisScopes(scope);
            anaScope.forEach(manager::collectDuplicatedComponents);
            eliminateDuplicatedData(manager::needRemove, manager::repOf);
        }
    }
}
