package pascal.hardscript.compiler.ast.expr.list;

import pascal.hardscript.compiler.ast.ASTListener;
import pascal.hardscript.compiler.ast.expr.ExprNode;

import java.util.List;

public record CommonList(List<ExprNode> elements) implements ListExpr {

    public static CommonList of(List<ExprNode> elements) {
        return new CommonList(elements);
    }

    public static CommonList ofEmpty() {
        return new CommonList(List.of());
    }

    @Override
    public void walk(ASTListener listener) {
        listener.enter(this);
        elements.forEach(element -> element.walk(listener));
        listener.exit(this);
    }
}
