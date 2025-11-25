package pascal.netx.compiler.analysis.ast;

import pascal.netx.compiler.ast.ASTListener;
import pascal.netx.compiler.ast.PackageNode;
import pascal.netx.compiler.ast.element.BindingNode;
import pascal.netx.compiler.ast.expr.CallSymbolExpr;
import pascal.netx.compiler.ast.expr.IdentifierExpr;
import pascal.netx.compiler.utils.Logger;
import pascal.netx.compiler.utils.ParseCancellationException;

/**
 * Checking whether CallSymbolExpr on the left-hand side of a binding contains no named bindings, i.e.,
 * though right-hand side named binding, e.g., {@code let foo = REGISTER(pos_edge=true)}, is legal,
 * the left-hand side should appear only in the form of {@code let foo(i) = i} rather than {@code let foo(i = j) = i}.
 */
public final class BindingLhsAnalysis extends ASTAnalysis implements ASTListener {
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
                    Logger.error(node.getSourceLoc(), "Unexpected assignment in macro parameter {}={}", assign.first(), assign.second());
                    throw new ParseCancellationException();
                }
                if (!(assign.second() instanceof IdentifierExpr)) {
                    Logger.error(node.getSourceLoc(), "Expected primary symbol but get {} as a marco parameter", assign.second());
                    throw new ParseCancellationException();
                }
            }
        }
    }
}
