package pascal.netx.compiler.analysis;

import pascal.netx.compiler.model.scope.ComponentScope;
import pascal.netx.compiler.model.scope.GlobalScope;

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
            signature.inputs().forEach(inst -> manager.setAsSourceRep(signature, inst));
            signature.outputs().forEach(inst -> manager.setAsSourceRep(signature, inst));
            eliminateDuplicatedData(Predicate.not(manager::needDump), manager::repOf);
        }
    }
}
