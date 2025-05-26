package pascal.netx.compiler.analysis.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pascal.netx.compiler.exception.CompileCancellationException;
import pascal.netx.compiler.model.scope.ComponentScope;
import pascal.netx.compiler.model.scope.PackageScope;
import pascal.netx.compiler.model.variable.Variable;
import pascal.netx.compiler.model.variable.selector.Selector;
import pascal.netx.compiler.model.variable.type.BitType;
import pascal.netx.util.lang.BuiltinComponent;

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
}
