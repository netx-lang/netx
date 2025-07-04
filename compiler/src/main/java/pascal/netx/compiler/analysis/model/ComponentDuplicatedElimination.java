package pascal.netx.compiler.analysis.model;

import pascal.netx.compiler.model.scope.ComponentScope;
import pascal.netx.compiler.model.scope.PackageScope;

/**
 * This analysis eliminates duplicated components within a component scope.
 * It identifies components that are structurally identical and replaces
 * them with a single representative instance, reducing redundancy in the model.
 * <p>
 * Used for compact dump of the design, where component instantiations are not expanded
 * and duplication introduced by them is not considered.
 */
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
            dominated.forEach(manager::collectDuplicatedComponents);
            manager.resolveRepresentatives();
            if (scope.hasAttributes()) {
                scope.getAttributes().replaceAllVariable(manager::repOf);
            }
            eliminateDuplicatedData(manager::needRemove, manager::repOf);
        }
    }
}
