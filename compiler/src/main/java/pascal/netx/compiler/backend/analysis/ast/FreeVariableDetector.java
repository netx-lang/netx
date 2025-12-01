package pascal.netx.compiler.backend.analysis.ast;


import pascal.netx.compiler.model.ast.ASTListener;
import pascal.netx.compiler.model.ast.expr.CallExpr;
import pascal.netx.compiler.model.ast.expr.ExprNode;
import pascal.netx.compiler.model.ast.expr.IdentifierExpr;
import pascal.netx.compiler.model.ast.expr.LetExpr;
import pascal.netx.util.lang.Identifier;

import java.util.HashSet;
import java.util.Set;

public final class FreeVariableDetector implements ASTListener {
    private final Set<Identifier> freeVariables = new HashSet<>();
    private final Set<Identifier> boundVariables = new HashSet<>();

    public static Set<Identifier> detect(ExprNode expr) {
        var detector = new FreeVariableDetector();
        expr.walk(detector);
        return detector.freeVariables;
    }

    @Override
    public void enter(LetExpr node) {
        var id = node.getBinding().getHead().getSymbol();
        boundVariables.add(id);
    }

    @Override
    public void exit(LetExpr node) {
        var id = node.getBinding().getHead().getSymbol();
        boundVariables.remove(id);
    }

    @Override
    public void enter(IdentifierExpr node) {
        var id = node.getSymbol();
        if (!boundVariables.contains(id)) {
            freeVariables.add(id);
        }
    }

    @Override
    public void enter(CallExpr node) {
        var id = node.getSymbol();
        if (!boundVariables.contains(id)) {
            freeVariables.add(id);
        }
    }
}
