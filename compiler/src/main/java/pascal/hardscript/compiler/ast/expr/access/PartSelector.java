package pascal.hardscript.compiler.ast.expr.access;

import pascal.hardscript.compiler.ast.expr.ExprNode;

public record PartSelector(ExprNode start, ExprNode end) implements Selector {

    public static PartSelector of(ExprNode start, ExprNode end) {
        return new PartSelector(start, end);
    }
}
