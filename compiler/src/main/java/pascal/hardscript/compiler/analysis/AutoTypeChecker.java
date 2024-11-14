package pascal.hardscript.compiler.analysis;

import pascal.hardscript.compiler.ast.ASTListener;
import pascal.hardscript.compiler.ast.DesignNode;
import pascal.hardscript.compiler.ast.component.ComponentNode;
import pascal.hardscript.compiler.ast.expr.AutoTypeExpr;

import java.util.HashMap;
import java.util.Map;

public class AutoTypeChecker extends ASTAnalysis {

    private final Map<ComponentNode, Boolean> hasAutoType = new HashMap<>();

    public AutoTypeChecker(DesignNode design) {
        super(design);
    }

    @Override
    public void analyze() {
        design.getComponents().forEach(component -> {
                    var checker = new Checker();
                    component.walk(checker);
                    hasAutoType.put(component, checker.hasAutoType());
                });
    }

    public Map<ComponentNode, Boolean> getAutoTyped() {
        return hasAutoType;
    }

    private static class Checker implements ASTListener {
        private boolean hasAutoType = false;

        public boolean hasAutoType() {
            return hasAutoType;
        }

        @Override
        public void enter(AutoTypeExpr node) {
            hasAutoType = true;
        }
    }
}
