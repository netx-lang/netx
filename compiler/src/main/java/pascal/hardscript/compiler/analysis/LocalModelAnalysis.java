package pascal.hardscript.compiler.analysis;

import pascal.hardscript.compiler.model.Entity;
import pascal.hardscript.compiler.model.instance.Instance;
import pascal.hardscript.compiler.model.scope.ComponentScope;
import pascal.hardscript.compiler.model.scope.DesignScope;
import pascal.hardscript.compiler.model.scope.Scope;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class LocalModelAnalysis extends ModelAnalysis {
    private final Consumer<Scope> analysis;

    public LocalModelAnalysis(DesignScope design, Consumer<Scope> analysis) {
        super(design);
        this.analysis = analysis;
    }

    private static boolean isUserDefinedComponent(Scope scope) {
        return !((ComponentScope) scope).isBuiltin();
    }

    @Override
    public void analyze() {
        design.forEachScope(scope -> {
            if (isUserDefinedComponent(scope)) {
                analysis.accept(scope);
            }
        });
    }

    protected abstract static class ComponentAnalysis {
        protected final Scope scope;
        protected final List<Scope> inlineScope = new LinkedList<>();
        protected final List<Scope> instantiateScope = new LinkedList<>();

        protected ComponentAnalysis(Scope scope) {
            this.scope = scope;
        }

        protected void exploreAnalysisScopes(Scope scope) {
            inlineScope.add(scope);
            for (var child : scope.getChildren()) {
                if (!isUserDefinedComponent(child)) {
                    exploreAnalysisScopes(child);
                } else {
                    instantiateScope.add(child);
                }
            }
        }

        public void eliminateDuplicatedData(Predicate<Entity> needRemove, Function<Instance, Instance> repOf) {
            inlineScope.forEach(scope -> {
                scope.getEntities().removeIf(needRemove);
                if (scope instanceof ComponentScope componentScope) {
                    componentScope.getInputs().replaceAll(repOf::apply);
                    componentScope.getOutputs().replaceAll(repOf::apply);
                }
            });

            instantiateScope.forEach(scope -> {
                if (scope instanceof ComponentScope componentScope) {
                    componentScope.getInputs().replaceAll(repOf::apply);
                    componentScope.getOutputs().replaceAll(repOf::apply);
                }
            });
        }
    }
}
