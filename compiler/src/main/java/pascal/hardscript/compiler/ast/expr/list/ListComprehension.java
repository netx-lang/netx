package pascal.hardscript.compiler.ast.expr.list;

import pascal.hardscript.compiler.ast.ASTListener;
import pascal.hardscript.compiler.ast.expr.ExprNode;

import java.util.List;

public record ListComprehension(ExprNode head, List<Qualifier> qualifiers) implements ListExpr {

    public static ListComprehension of(ExprNode head, List<Qualifier> qualifiers) {
        return new ListComprehension(head, qualifiers);
    }

    @Override
    public void walk(ASTListener listener) {
        listener.enter(this);
        head.walk(listener);
        qualifiers.forEach(qualifier -> qualifier.walk(listener));
        listener.exit(this);
    }
}
