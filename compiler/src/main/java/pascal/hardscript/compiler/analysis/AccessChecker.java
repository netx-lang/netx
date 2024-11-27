package pascal.hardscript.compiler.analysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pascal.hardscript.compiler.exception.TypeCheckingException;
import pascal.hardscript.compiler.model.Entity;
import pascal.hardscript.compiler.model.instance.DataAccess;
import pascal.hardscript.compiler.model.instance.IntegerValue;
import pascal.hardscript.compiler.model.instance.selector.BitSelector;
import pascal.hardscript.compiler.model.instance.selector.PartSelector;
import pascal.hardscript.compiler.model.instance.type.BitType;
import pascal.hardscript.compiler.model.scope.ComponentScope;
import pascal.hardscript.compiler.model.scope.DesignScope;
import pascal.hardscript.compiler.model.scope.Scope;

public class AccessChecker extends ModelAnalysis {

    private static final Logger log = LoggerFactory.getLogger(AccessChecker.class);

    public AccessChecker(DesignScope design) {
        super(design);
    }

    private static void check(Entity entity) {
        if (entity instanceof DataAccess(var base, var selector)) {
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
        design.forEachSubScope(this::analyze);
    }

    private void analyze(Scope scope) {
        if (scope instanceof ComponentScope component) {
            component.getInputs().forEach(AccessChecker::check);
            component.getOutputs().forEach(AccessChecker::check);
            component.getBinding().getEntities().forEach(AccessChecker::check);
            component.getAliases().forEach(alias -> {
                check(alias.lhs());
                check(alias.rhs());
            });
        }
    }
}
