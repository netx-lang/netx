package pascal.hardscript.compiler.analysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pascal.hardscript.compiler.ast.ASTListener;
import pascal.hardscript.compiler.ast.DesignNode;
import pascal.hardscript.compiler.ast.component.BindingNode;
import pascal.hardscript.compiler.ast.expr.IdentifierExpr;
import pascal.hardscript.compiler.ast.expr.ParamSymbolExpr;
import pascal.hardscript.compiler.exception.CompileCancellationException;

public final class BindingLhsAnalysis extends ASTAnalysis implements ASTListener {

    private static final Logger log = LoggerFactory.getLogger(BindingLhsAnalysis.class);

    public BindingLhsAnalysis(DesignNode design) {
        super(design);
    }

    @Override
    public void analyze() {
        design.walk(this);
    }

    @Override
    public void enter(BindingNode node) {
        if (node.getHead() instanceof ParamSymbolExpr param) {
            for (var arg : param.getArguments()) {
                if (!(arg instanceof IdentifierExpr)) {
                    log.error("{}: Expected primary symbol but get {} as a marco parameter", node.getSourceLoc(), arg);
                    throw new CompileCancellationException();
                }
            }
        }
    }
}
