package pascal.netx.compiler.analysis;

import pascal.netx.compiler.model.scope.ComponentScope;
import pascal.netx.compiler.model.scope.PackageScope;

import java.util.function.Predicate;

public final class ComponentAliasAnalysis extends ComponentModelAnalysis {

    public ComponentAliasAnalysis(PackageScope design) {
        super(design, scope -> new Analyzer(scope).analyze());
    }

    private static class Analyzer extends ComponentModelAnalysis.Analyzer {
        private final AliasManager manager = new AliasManager();

        private Analyzer(ComponentScope scope) {
            super(scope);
        }

        public void analyze() {
            exploreAnalysisScopes(scope);
            dominated.stream()
                    .map(ComponentScope::getAliases)
                    .forEach(manager::addAlias);
            manager.resolveRepresentatives();
            var signature = scope.getSignature();
            signature.inputs().forEach(inst -> manager.setAsSourceRep(signature, inst));
            signature.outputs().forEach(inst -> manager.setAsSourceRep(signature, inst));
            if (scope.hasAttributes()) {
                scope.getAttributes().replaceAllVariable(manager::repOf);
            }
            eliminateDuplicatedData(Predicate.not(manager::needDump), manager::repOf);
        }
    }
}
