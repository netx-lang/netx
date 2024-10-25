package pascal.hardscript.compiler.ast.expr.list;

import pascal.hardscript.compiler.ast.ASTListener;
import pascal.hardscript.compiler.ast.expr.ExprNode;

public record FilterQualifier(ExprNode condition) implements Qualifier {

    public static FilterQualifier of(ExprNode condition) {
        return new FilterQualifier(condition);
    }

    @Override
    public void walk(ASTListener listener) {
        listener.enter(this);
        condition.walk(listener);
        listener.exit(this);
    }
}
