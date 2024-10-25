package pascal.hardscript.compiler.ast.expr.access;

import pascal.hardscript.compiler.ast.ASTListener;
import pascal.hardscript.compiler.ast.expr.ExprNode;

public record BitSelector(ExprNode index) implements Selector {

    public static BitSelector of(ExprNode index) {
        return new BitSelector(index);
    }

    @Override
    public void walk(ASTListener listener) {
        listener.enter(this);
        index.walk(listener);
        listener.exit(this);
    }
}
