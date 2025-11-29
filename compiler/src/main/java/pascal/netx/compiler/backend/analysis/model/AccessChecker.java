package pascal.netx.compiler.backend.analysis.model;

import pascal.netx.compiler.circuit.Entity;
import pascal.netx.compiler.circuit.scope.ComponentScope;
import pascal.netx.compiler.circuit.scope.PackageScope;
import pascal.netx.compiler.circuit.signal.Access;
import pascal.netx.compiler.circuit.signal.selector.BitSelector;
import pascal.netx.compiler.circuit.signal.selector.PartSelector;
import pascal.netx.compiler.circuit.signal.type.BitType;
import pascal.netx.compiler.io.Logger;
import pascal.netx.compiler.utils.TypeCheckingException;

/**
 * Checks the validity of access operations in the design.
 * This includes ensuring that indices and ranges used in access operations.
 */
public final class AccessChecker extends ModelAnalysis {

    public AccessChecker(PackageScope design) {
        super(design);
    }

    private static void check(Entity entity) {
        if (entity instanceof Access(var base, var selector)) {
            var baseWidth = ((BitType) base.getType()).width();
            switch (selector) {
                case BitSelector(var index) -> {
                    if (check(index, baseWidth)) return;
                }
                case PartSelector(var start, var end) -> {
                    if (check(start, baseWidth) && check(end, baseWidth)) return;
                }
            }
            Logger.error(base.getSourceLoc(), "Invalid access to {}{}", base, selector);
            throw new TypeCheckingException();
        }
    }

    private static boolean check(int index, int width) {
        return 0 <= index && index < width;
    }

    @Override
    public void analyze() {
        pack.forEachSubComponent(this::analyze);
    }

    private void analyze(ComponentScope scope) {
        scope.getInputs().forEach(AccessChecker::check);
        scope.getOutputs().forEach(AccessChecker::check);
        scope.getBinding().forEachEntity(AccessChecker::check);
        if (scope.hasAttributes()) {
            for (var entry : scope.getAttributes().entrySet()) {
                check(entry.getValue());
            }
        }
        scope.getAliases().forEach(alias -> {
            check(alias.lhs());
            check(alias.rhs());
        });
    }
}
