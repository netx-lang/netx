package pascal.netx.compiler.analysis.model;

import pascal.netx.compiler.model.Entity;
import pascal.netx.compiler.model.scope.ComponentScope;
import pascal.netx.compiler.model.scope.PackageScope;
import pascal.netx.compiler.model.variable.Variable;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Base class for intra-component analysis (e.g., {@link ComponentAliasAnalysis}, {@link ComponentDuplicatedElimination}).
 * Provide an analyzer find the analysis scopes that contains a user-defined component
 * and multiple builtin components instantiated in it, leaving other user-defined subcomponents out.
 */
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
         * Dominated scopes are those fully influenced by the aliases in current scope,
         * including the scope itself, instantiated builtin components, and interfaces.
         */
        protected final List<ComponentScope> dominated = new LinkedList<>();

        /**
         * IO related scopes are those whose inputs and outputs are influenced by the aliases in current scope,
         * including the instantiated user-defined components.
         */
        protected final List<ComponentScope> ioRelated = new LinkedList<>();

        protected Analyzer(ComponentScope scope) {
            this.scope = scope;
        }

        protected void exploreAnalysisScopes(ComponentScope scope) {
            dominated.add(scope);
            for (var kid : scope.getChildren()) {
                var child = (ComponentScope) kid;
                if (child.isBuiltin() || child.isInterface()) {
                    dominated.add(child);
                } else {
                    ioRelated.add(child);
                }
            }
        }

        public void eliminateDuplicatedData(Predicate<Entity> needRemove, Function<Variable, Variable> repOf) {
            dominated.forEach(scope -> {
                scope.getBinding().releaseIf(needRemove);
                scope.getInputs().replaceAll(repOf::apply);
                scope.getOutputs().replaceAll(repOf::apply);
            });
            ioRelated.forEach(scope -> {
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
