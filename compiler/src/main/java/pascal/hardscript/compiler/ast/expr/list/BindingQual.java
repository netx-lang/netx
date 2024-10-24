package pascal.hardscript.compiler.ast.expr.list;

import pascal.hardscript.compiler.ast.Identifier;
import pascal.hardscript.compiler.ast.expr.ExprNode;

public record BindingQual(Identifier head, ExprNode expr) implements Qualifier {

    public static BindingQual of(Identifier head, ExprNode expr) {
        return new BindingQual(head, expr);
    }
}
