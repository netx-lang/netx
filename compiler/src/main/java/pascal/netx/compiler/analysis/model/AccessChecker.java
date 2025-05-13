package pascal.netx.compiler.analysis.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pascal.netx.compiler.exception.TypeCheckingException;
import pascal.netx.compiler.model.Entity;
import pascal.netx.compiler.model.scope.ComponentScope;
import pascal.netx.compiler.model.scope.PackageScope;
import pascal.netx.compiler.model.variable.Access;
import pascal.netx.compiler.model.variable.IntegerValue;
import pascal.netx.compiler.model.variable.selector.BitSelector;
import pascal.netx.compiler.model.variable.selector.PartSelector;
import pascal.netx.compiler.model.variable.type.BitType;

public class AccessChecker extends ModelAnalysis {

    private static final Logger log = LoggerFactory.getLogger(AccessChecker.class);

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
            log.error("{}: Invalid access to {}{}", base.getSourceLoc(), base, selector);
            throw new TypeCheckingException();
        }
    }

    private static boolean check(IntegerValue index, int width) {
        var value = index.toInt();
        return 0 <= value && value < width;
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
