package pascal.hardscript.compiler.ast.expr.list;

import pascal.hardscript.compiler.ast.expr.ExprNode;

import java.util.List;

public record ListComprehension(ExprNode head, List<Qualifier> qualifiers) implements ListExpr {

    public static ListComprehension of(ExprNode head, List<Qualifier> qualifiers) {
        return new ListComprehension(head, qualifiers);
    }
}
