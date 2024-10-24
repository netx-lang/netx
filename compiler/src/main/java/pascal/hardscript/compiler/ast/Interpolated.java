package pascal.hardscript.compiler.ast;

import pascal.hardscript.compiler.ast.expr.ExprNode;

import java.util.List;

public record Interpolated(List<String> fragments, List<ExprNode> exprs) implements Symbol {

    public static Interpolated of(List<String> fragments, List<ExprNode> exprs) {
        return new Interpolated(fragments, exprs);
    }
}
