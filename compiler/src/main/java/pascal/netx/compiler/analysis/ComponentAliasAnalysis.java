package pascal.netx.compiler.analysis;

import pascal.netx.compiler.model.scope.ComponentScope;
import pascal.netx.compiler.model.scope.ModuleScope;

import java.util.function.Predicate;

public final class ComponentAliasAnalysis extends ComponentModelAnalysis {

    public ComponentAliasAnalysis(ModuleScope design) {
        super(design, scope -> new Analyzer(scope).analyze());
    }

    private static class Analyzer extends ComponentModelAnalysis.Analyzer {
        private final AliasManager manager = new AliasManager();

        private Analyzer(ComponentScope scope) {
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
