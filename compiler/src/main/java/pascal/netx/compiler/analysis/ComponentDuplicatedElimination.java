package pascal.netx.compiler.analysis;

import pascal.netx.compiler.analysis.constraint.DuplicatedManager;
import pascal.netx.compiler.model.scope.ComponentScope;
import pascal.netx.compiler.model.scope.PackageScope;

public final class ComponentDuplicatedElimination extends ComponentModelAnalysis {

    public ComponentDuplicatedElimination(PackageScope design) {
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
            if (scope.hasAttributes()) {
                scope.getAttributes().replaceAllVariable(manager::repOf);
            }
            eliminateDuplicatedData(manager::needRemove, manager::repOf);
        }
    }
}
