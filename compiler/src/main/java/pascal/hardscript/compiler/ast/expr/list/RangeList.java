package pascal.hardscript.compiler.ast.expr.list;

import pascal.hardscript.compiler.ast.ASTListener;
import pascal.hardscript.compiler.ast.expr.ExprNode;

public record RangeList(ExprNode from, ExprNode to) implements ListExpr {

    public static RangeList of(ExprNode from, ExprNode to) {
        return new RangeList(from, to);
    }

    @Override
    public void walk(ASTListener listener) {
        listener.enter(this);
        from.walk(listener);
        to.walk(listener);
        listener.exit(this);
    }
}
