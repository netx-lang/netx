package pascal.hardscript.compiler.analysis;

import pascal.hardscript.compiler.model.scope.ComponentScope;
import pascal.hardscript.compiler.model.scope.GlobalScope;

import java.util.function.Predicate;

public final class LocalAliasAnalysis extends LocalModelAnalysis {

    public LocalAliasAnalysis(GlobalScope design) {
        super(design, scope -> new ComponentAliasAnalysis(scope).analyze());
    }

    private static class ComponentAliasAnalysis extends ComponentAnalysis {
        private final AliasManager manager = new AliasManager();

        private ComponentAliasAnalysis(ComponentScope scope) {
            super(scope);
        }

        public void analyze() {
            exploreAnalysisScopes(scope);
            anaScope.stream()
                    .map(ComponentScope::getAliases)
                    .forEach(manager::addAlias);
            manager.resolveRepresentatives();
            var signature = scope.getSignature();
            signature.inputs().forEach(manager::setAsSourceRep);
            signature.outputs().forEach(manager::setAsSourceRep);
            eliminateDuplicatedData(Predicate.not(manager::needDump), manager::repOf);
        }
    }
}
