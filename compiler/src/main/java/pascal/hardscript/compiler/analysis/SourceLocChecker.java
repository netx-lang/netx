package pascal.hardscript.compiler.analysis;

import pascal.hardscript.compiler.ast.ASTListener;
import pascal.hardscript.compiler.ast.ASTNode;
import pascal.hardscript.compiler.ast.DesignNode;
import pascal.hardscript.compiler.ast.expr.IdentifierExpr;

import java.util.Objects;

public final class SourceLocChecker extends ASTAnalysis implements ASTListener {

    public SourceLocChecker(DesignNode design) {
        super(design);
    }

    @Override
    public void analyze() {
        design.walk(this);
    }

    @Override
    public void enterDefault(ASTNode node) {
        Objects.requireNonNull(node.getSourceLoc());
    }

    @Override
    public void enter(IdentifierExpr node) {
        Objects.requireNonNull(node.getIdentifier().getSourceLoc());
    }
}
