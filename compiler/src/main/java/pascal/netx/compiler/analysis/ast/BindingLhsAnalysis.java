package pascal.netx.compiler.analysis.ast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pascal.netx.compiler.ast.ASTListener;
import pascal.netx.compiler.ast.PackageNode;
import pascal.netx.compiler.ast.element.BindingNode;
import pascal.netx.compiler.ast.expr.CallSymbolExpr;
import pascal.netx.compiler.ast.expr.IdentifierExpr;
import pascal.netx.compiler.exception.CompileCancellationException;

/**
 * Since CallSymbolExpr can be used on both
 * the left-hand side (e.g., let foo(i) = i;), and
 * the right-hand side (e.g., let foo = REGISTER(pos_edge=true);),
 * we need to ensure the CallSymbolExpr on the left-hand-side is legal.
 */
public final class BindingLhsAnalysis extends ASTAnalysis implements ASTListener {

    private static final Logger log = LoggerFactory.getLogger(BindingLhsAnalysis.class);

    public BindingLhsAnalysis(PackageNode design) {
        super(design);
    }

    @Override
    public void analyze() {
        design.walk(this);
    }

    @Override
    public void enter(BindingNode node) {
        if (node.getHead() instanceof CallSymbolExpr param) {
            for (var assign : param.getAssigns()) {
                if (assign.first() != null) {
                    log.error("{}: Unexpected assignment in macro parameter {}={}",
                            node.getSourceLoc(), assign.first(), assign.second());
                    throw new CompileCancellationException();
                }
                if (!(assign.second() instanceof IdentifierExpr)) {
                    log.error("{}: Expected primary symbol but get {} as a marco parameter",
                            node.getSourceLoc(), assign.second());
                    throw new CompileCancellationException();
                }
            }
        }
    }
}
