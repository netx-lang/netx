package pascal.hardscript.compiler.ast.expr;

import pascal.hardscript.compiler.ast.expr.atom.AtomExpr;

public record ConditionalExpr(AtomExpr condition, ExprNode thenBranch, ExprNode elseBranch) implements ExprNode {

    public static ConditionalExpr of(AtomExpr condition, ExprNode thenBranch, ExprNode elseBranch) {
        return new ConditionalExpr(condition, thenBranch, elseBranch);
    }
}
