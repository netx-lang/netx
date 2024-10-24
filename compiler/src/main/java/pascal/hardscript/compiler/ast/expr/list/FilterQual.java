package pascal.hardscript.compiler.ast.expr.list;

import pascal.hardscript.compiler.ast.expr.ExprNode;

public record FilterQual(ExprNode condition) implements Qualifier {

    public static FilterQual of(ExprNode condition) {
        return new FilterQual(condition);
    }
}
