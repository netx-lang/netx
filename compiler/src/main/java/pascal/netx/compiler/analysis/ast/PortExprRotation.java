package pascal.netx.compiler.analysis.ast;

import pascal.netx.compiler.ast.ASTListener;
import pascal.netx.compiler.ast.PackageNode;
import pascal.netx.compiler.ast.expr.BinaryExpr;
import pascal.netx.compiler.ast.expr.PortConnectionExpr;

public class PortExprRotation extends ASTAnalysis implements ASTListener {

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
                    node.getRhs() instanceof PortConnectionExpr) {
                node.setLhs(tail.getLhs());
                node.setRhs(BinaryExpr.of(BinaryExpr.Operator.SEQ, tail.getRhs(), node.getRhs()));
            }
        }
    }
}
