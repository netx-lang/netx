package pascal.netx.compiler.backend.analysis.ast;

import pascal.netx.compiler.ast.ASTListener;
import pascal.netx.compiler.ast.PackageNode;
import pascal.netx.compiler.ast.expr.BinaryExpr;
import pascal.netx.compiler.ast.expr.NamedPortExpr;

/**
 * Rotates port expressions in sequential connections.
 * This analysis helps further elaboration identifying which port to assign with
 * when there is a named connection such as
 * <p>
 * <code>
 * {name : value} <> some_component <> {name : value}
 * </code>
 * </p>
 * by rotating some_component as the root of the AST with each named binding being its child.
 */
public final class PortExprRotation extends ASTAnalysis implements ASTListener {

    public PortExprRotation(PackageNode design) {
        super(design);
    }

    @Override
    public void analyze() {
        design.walk(this);
    }

    @Override
    public void enter(BinaryExpr node) {
        if (node.getOp() == BinaryExpr.Operator.SEQ) {
            if (node.getLhs() instanceof BinaryExpr tail &&
                    node.getRhs() instanceof NamedPortExpr) {
                node.setLhs(tail.getLhs());
                node.setRhs(BinaryExpr.of(BinaryExpr.Operator.SEQ, tail.getRhs(), node.getRhs()));
            }
        }
    }
}
