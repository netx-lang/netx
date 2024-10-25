package pascal.hardscript.compiler.ast.module;

import pascal.hardscript.compiler.ast.Symbol;
import pascal.hardscript.compiler.ast.expr.ExprNode;

import java.util.List;

public record ComponentNode(List<Symbol> ids, ExprNode type) implements ElementNode {

    public static ComponentNode of(List<Symbol> ids, ExprNode type) {
        return new ComponentNode(ids, type);
    }
}
