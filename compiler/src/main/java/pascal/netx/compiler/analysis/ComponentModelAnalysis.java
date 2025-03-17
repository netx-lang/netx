package pascal.netx.compiler.analysis;

import pascal.netx.compiler.model.Entity;
import pascal.netx.compiler.model.scope.ComponentScope;
import pascal.netx.compiler.model.scope.PackageScope;
import pascal.netx.compiler.model.variable.Variable;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class ComponentModelAnalysis extends ModelAnalysis {
    private final Consumer<ComponentScope> analysis;

    public ComponentModelAnalysis(PackageScope design, Consumer<ComponentScope> analysis) {
        super(design);
        this.analysis = analysis;
    }

    @Override
    public void analyze() {
        pack.forEachSubComponent(scope -> {
            if (!scope.isBuiltin()) {
                analysis.accept(scope);
            }
        });
    }

    protected abstract static class Analyzer {
        protected final ComponentScope scope;

        /**
         * Analysis scopes including the scope itself and its sub-scopes standing for builtin components;
         */
        protected final List<ComponentScope> anaScope = new LinkedList<>();

        /**
         * User defined sub-scopes.
         */
        protected final List<ComponentScope> subUserDef = new LinkedList<>();

        protected Analyzer(ComponentScope scope) {
            this.scope = scope;
        }

        protected void exploreAnalysisScopes(ComponentScope scope) {
            anaScope.add(scope);
            for (var kid : scope.getChildren()) {
                var child = (ComponentScope) kid;
                if (child.isBuiltin()) {
                    anaScope.add(child);
                } else {
                    subUserDef.add(child);
                }
            }
        }

        public void eliminateDuplicatedData(Predicate<Entity> needRemove, Function<Variable, Variable> repOf) {
            anaScope.forEach(scope -> {
                scope.getBinding().releaseIf(needRemove);
                scope.getInputs().replaceAll(repOf::apply);
                scope.getOutputs().replaceAll(repOf::apply);
            });
            subUserDef.forEach(scope -> {
                scope.getInputs().replaceAll(repOf::apply);
                scope.getOutputs().replaceAll(repOf::apply);
            });
            scope.getSignature().inputs().replaceAll(repOf::apply);
            scope.getSignature().outputs().replaceAll(repOf::apply);

            if (scope.hasAttributes()) {
                for (var entry : scope.getAttributes().entrySet()) {
                    if (entry.getValue() instanceof Variable variable) {
                        entry.setValue(repOf.apply(variable));
                    }
                }
            }
        }
    }
}
