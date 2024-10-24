package pascal.hardscript.compiler.ast.expr.list;

import pascal.hardscript.compiler.ast.expr.ExprNode;

public record FilterQualifier(ExprNode condition) implements Qualifier {

    public static FilterQualifier of(ExprNode condition) {
        return new FilterQualifier(condition);
    }
}
