package pascal.netx.compiler.analysis.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pascal.netx.compiler.exception.CompileCancellationException;
import pascal.netx.compiler.model.scope.ComponentScope;
import pascal.netx.compiler.model.scope.PackageScope;
import pascal.netx.compiler.model.variable.Variable;
import pascal.netx.compiler.model.variable.Wire;
import pascal.netx.compiler.model.variable.selector.Selector;
import pascal.netx.compiler.model.variable.type.BitType;
import pascal.netx.util.collection.UnionFind;
import pascal.netx.util.lang.BuiltinComponent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This analysis extracts alias relations from built-in components.
 * There are three cases:
 * <li> In {@code CAST} components, the input is truncated, extended, or directly passed to the output;
 * we explicitly add the alias relation between the output and the input (or a truncated input).
 * <li> In {@code CONCAT} components, if there is only one input,
 * we add an alias relation between the output and the input since there is no concatenation actually happening.
 * <li> In {@code TO} components, the elaborator introduces relations
 * like {@code i <> $TO_DUMMY}. and {@code $TO_DUMMY <> o};
 * we inline these relations to make them direct aliases {@code i <> o}.
 */
public class BuiltinAliasAnalysis extends ModelAnalysis {
    private static final Logger log = LoggerFactory.getLogger(BuiltinAliasAnalysis.class);

    public BuiltinAliasAnalysis(PackageScope pack) {
        super(pack);
    }

    @Override
    public void analyze() {
        pack.forEachSubComponent(scope -> {
            if (scope.isBuiltin()) {
                var builtinRef = BuiltinComponent.of(scope.getIdentifier());
                switch (builtinRef) {
                    case CAST -> extractFromCast(scope);
                    case CONCAT -> extractFromConcat(scope);
                }
            }
        });
        pack.forEachSubComponent(this::inlineIndirectToAlias);
    }

    private void extractFromCast(ComponentScope scope) {
        var lhs = scope.getOutputs().getFirst();
        var rhs = scope.getInputs().getFirst();
        if (!(lhs.getType() instanceof BitType(int lhsW)) || !(rhs.getType() instanceof BitType(int rhsW))) {
            log.warn("Unexpected type for CAST component: {} -> {}", lhs.getType(), rhs.getType());
            throw new CompileCancellationException();
        }
        if (lhsW == rhsW) {
            scope.getAliases().add(new ComponentScope.Alias(lhs, rhs));
        } else if (lhsW < rhsW) {
            var truncated = Variable.accessOf(rhs, Selector.ofWidth(lhsW));
            scope.getAliases().add(new ComponentScope.Alias(lhs, truncated));
            log.info("Truncating {} to {}", rhs, truncated);
        }
    }

    private void extractFromConcat(ComponentScope scope) {
        if (scope.getInputs().size() == 1) {
            var lhs = scope.getOutputs().getFirst();
            var rhs = scope.getInputs().getFirst();
            scope.getAliases().add(new ComponentScope.Alias(lhs, rhs));
        }
    }

    /**
     * In TO components, there are alias relations i -> dummy and dummy -> o.
     * This indirect alias may cause further problems, so we inline them here.
     */
    private void inlineIndirectToAlias(ComponentScope scope) {
        var dummies = collectDummies(scope);

        // Collect non-dummy <-> dummy relations
        var dummyToRhs = collectNonDummies(scope, dummies, true);
        var dummyToLhs = collectNonDummies(scope, dummies, false);

        // Build direct alias for non-dummy <-> non-dummy relations
        for (var dummy : dummies.representatives()) {
            var lhsList = dummyToLhs.get(dummy);
            var rhsList = dummyToRhs.get(dummy);
            if (lhsList == null || rhsList == null) continue;
            for (var lhs : lhsList) {
                for (var rhs : rhsList) {
                    scope.getAliases().add(new ComponentScope.Alias(lhs, rhs));
                }
            }
        }
    }

    // Find all dummies and group them using UnionFind
    private UnionFind<Variable> collectDummies(ComponentScope scope) {
        var dummies = new UnionFind<Variable>();
        for (var alias : scope.getAliases()) {
            boolean dummyLhs = isToDummy(alias.lhs());
            boolean dummyRhs = isToDummy(alias.rhs());
            if (dummyLhs) dummies.add(alias.lhs());
            if (dummyRhs) dummies.add(alias.rhs());
            if (dummyLhs && dummyRhs) dummies.union(alias.lhs(), alias.rhs());
        }
        return dummies;
    }

    private HashMap<Variable, List<Variable>> collectNonDummies(
            ComponentScope scope, UnionFind<Variable> dummies, boolean lhsIsDummy) {
        var dummyToOther = new HashMap<Variable, List<Variable>>();
        for (var alias : scope.getAliases()) {
            Variable dummy = lhsIsDummy ? alias.lhs() : alias.rhs();
            Variable other = lhsIsDummy ? alias.rhs() : alias.lhs();
            if (isToDummy(dummy) && !isToDummy(other)) {
                dummyToOther.computeIfAbsent(dummies.find(dummy), _ -> new ArrayList<>()).add(other);
            }
        }
        return dummyToOther;
    }

    private boolean isToDummy(Variable var) {
        return var instanceof Wire wire && wire.getIdentifier().name().equals("$TO_DUMMY");
    }
}
