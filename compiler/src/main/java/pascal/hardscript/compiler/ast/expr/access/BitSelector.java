package pascal.hardscript.compiler.ast.expr.access;

import pascal.hardscript.compiler.ast.expr.ExprNode;

public record BitSelector(ExprNode index) implements Selector {

    public static BitSelector of(ExprNode index) {
        return new BitSelector(index);
    }
}
