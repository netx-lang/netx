package pascal.hardscript.compiler.ast.expr.access;

import pascal.hardscript.compiler.ast.ASTListener;
import pascal.hardscript.compiler.ast.expr.ExprNode;

public record PartSelector(ExprNode start, ExprNode end) implements Selector {

    public static PartSelector of(ExprNode start, ExprNode end) {
        return new PartSelector(start, end);
    }

    @Override
    public void walk(ASTListener listener) {
        listener.enter(this);
        start.walk(listener);
        end.walk(listener);
        listener.exit(this);
    }
}
