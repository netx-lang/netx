package pascal.hardscript.compiler.analysis;

import pascal.hardscript.compiler.model.scope.ComponentScope;
import pascal.hardscript.compiler.model.scope.DesignScope;
import pascal.hardscript.compiler.model.scope.Scope;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

public final class AliasAnalysis extends ModelAnalysis {
    private final AliasStructure global = new AliasStructure();

    public AliasAnalysis(DesignScope design) {
        super(design);
    }

    @Override
    public void analyze() {
        design.forEachScope(scope -> {
            if (isUserDefinedComponent(scope)) {
                new ComponentAnalysis(scope).analyze();
            }
        });
        global.resolveRepresentatives();
        design.forEachScope(scope -> {
            if (isUserDefinedComponent(scope)) {
                var component = (ComponentScope) scope;
                component.getInputs().replaceAll(global::repOf);
                component.getOutputs().replaceAll(global::repOf);
            }
        });
    }

    private static boolean isUserDefinedComponent(Scope scope) {
        return scope instanceof ComponentScope component && !component.isBuiltin();
    }

    private class ComponentAnalysis {
        private final Scope scope;
        private final AliasStructure local = new AliasStructure();
        private final List<Scope> analysisScopes = new LinkedList<>();

        private ComponentAnalysis(Scope scope) {
            this.scope = scope;
        }

        public void analyze() {
            exploreAnalysisScopes(scope);
            local.resolveRepresentatives();
            eliminateDuplicatedData();
        }

        private void exploreAnalysisScopes(Scope scope) {
            analysisScopes.add(scope);
            local.addAlias(scope.getAliases());
            global.addAlias(scope.getAliases());
            for (var child : scope.getChildren()) {
                if (!isUserDefinedComponent(child)) {
                    exploreAnalysisScopes(child);
                }
            }
        }

        public void eliminateDuplicatedData() {
            analysisScopes.forEach(scope -> {
                scope.getEntities().removeIf(Predicate.not(local::needDump));
                if (scope instanceof ComponentScope componentScope) {
                    componentScope.getInputs().replaceAll(local::repOf);
                    componentScope.getOutputs().replaceAll(local::repOf);
                }
            });
        }
    }

}
