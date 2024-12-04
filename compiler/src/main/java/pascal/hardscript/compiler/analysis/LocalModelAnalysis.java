package pascal.hardscript.compiler.analysis;

import pascal.hardscript.compiler.model.Entity;
import pascal.hardscript.compiler.model.instance.Instance;
import pascal.hardscript.compiler.model.scope.ComponentScope;
import pascal.hardscript.compiler.model.scope.GlobalScope;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class LocalModelAnalysis extends ModelAnalysis {
    private final Consumer<ComponentScope> analysis;

    public LocalModelAnalysis(GlobalScope design, Consumer<ComponentScope> analysis) {
        super(design);
        this.analysis = analysis;
    }

    @Override
    public void analyze() {
        design.forEachSubScope(scope -> {
            if (!scope.isBuiltin()) {
                analysis.accept(scope);
            }
        });
    }

    protected abstract static class ComponentAnalysis {
        protected final ComponentScope scope;

        /**
         * Analysis scopes including the scope itself and its sub-scopes standing for builtin components;
         */
        protected final List<ComponentScope> anaScope = new LinkedList<>();

        /**
         * User defined sub-scopes.
         */
        protected final List<ComponentScope> subUserDef = new LinkedList<>();

        protected ComponentAnalysis(ComponentScope scope) {
            this.scope = scope;
        }

        protected void exploreAnalysisScopes(ComponentScope scope) {
            anaScope.add(scope);
            for (var child : scope.getChildren()) {
                if (child.isBuiltin()) {
                    anaScope.add(child);
                } else {
                    subUserDef.add(child);
                }
            }
        }

        public void eliminateDuplicatedData(Predicate<Entity> needRemove, Function<Instance, Instance> repOf) {
            anaScope.forEach(scope -> {
                scope.getEntities().removeIf(needRemove);
                scope.getInputs().replaceAll(repOf::apply);
                scope.getOutputs().replaceAll(repOf::apply);
            });
            subUserDef.forEach(scope -> {
                scope.getInputs().replaceAll(repOf::apply);
                scope.getOutputs().replaceAll(repOf::apply);
            });
            scope.getSignature().inputs().replaceAll(repOf::apply);
            scope.getSignature().outputs().replaceAll(repOf::apply);
        }
    }
}
