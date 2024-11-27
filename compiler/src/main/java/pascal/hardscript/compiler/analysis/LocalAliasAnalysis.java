package pascal.hardscript.compiler.analysis;

import pascal.hardscript.compiler.model.scope.ComponentScope;
import pascal.hardscript.compiler.model.scope.DesignScope;
import pascal.hardscript.compiler.model.scope.Scope;

import java.util.function.Predicate;

public final class LocalAliasAnalysis extends LocalModelAnalysis {

    public LocalAliasAnalysis(DesignScope design) {
        super(design, scope -> new ComponentAliasAnalysis(scope).analyze());
    }

    private static class ComponentAliasAnalysis extends ComponentAnalysis {
        private final AliasManager manager = new AliasManager();

        private ComponentAliasAnalysis(Scope scope) {
            super(scope);
        }

        public void analyze() {
            exploreAnalysisScopes(scope);
            inlineScope.stream()
                    .map(ComponentScope.class::cast)
                    .map(ComponentScope::getAliases)
                    .forEach(manager::addAlias);
            manager.resolveRepresentatives();
            eliminateDuplicatedData(Predicate.not(manager::needDump), manager::repOf);
        }
    }
}
