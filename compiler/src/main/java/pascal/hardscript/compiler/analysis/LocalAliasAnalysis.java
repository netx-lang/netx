package pascal.hardscript.compiler.analysis;

import pascal.hardscript.compiler.model.scope.ComponentScope;
import pascal.hardscript.compiler.model.scope.DesignScope;
import pascal.hardscript.compiler.model.scope.Scope;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

public final class LocalAliasAnalysis extends ModelAnalysis {

    public LocalAliasAnalysis(DesignScope design) {
        super(design);
    }

    private static boolean isUserDefinedComponent(Scope scope) {
        return !((ComponentScope) scope).isBuiltin();
    }

    @Override
    public void analyze() {
        design.forEachScope(scope -> {
            if (isUserDefinedComponent(scope)) {
                new ComponentScopeWiseAnalysis(scope).analyze();
            }
        });
    }

    private static class ComponentScopeWiseAnalysis {
        private final Scope scope;
        private final AliasStructure local = new AliasStructure();
        private final List<Scope> inlineScope = new LinkedList<>();
        private final List<Scope> instantiateScope = new LinkedList<>();

        private ComponentScopeWiseAnalysis(Scope scope) {
            this.scope = scope;
        }

        public void analyze() {
            exploreAnalysisScopes(scope);
            local.resolveRepresentatives();
            eliminateDuplicatedData();
        }

        private void exploreAnalysisScopes(Scope scope) {
            inlineScope.add(scope);
            local.addAlias(scope.getAliases());
            for (var child : scope.getChildren()) {
                if (!isUserDefinedComponent(child)) {
                    exploreAnalysisScopes(child);
                } else {
                    instantiateScope.add(child);
                }
            }
        }

        public void eliminateDuplicatedData() {
            inlineScope.forEach(scope -> {
                scope.getEntities().removeIf(Predicate.not(local::needDump));
                if (scope instanceof ComponentScope componentScope) {
                    componentScope.getInputs().replaceAll(local::repOf);
                    componentScope.getOutputs().replaceAll(local::repOf);
                }
            });

            instantiateScope.forEach(scope -> {
                if (scope instanceof ComponentScope componentScope) {
                    componentScope.getInputs().replaceAll(local::repOf);
                    componentScope.getOutputs().replaceAll(local::repOf);
                }
            });
        }
    }
}
