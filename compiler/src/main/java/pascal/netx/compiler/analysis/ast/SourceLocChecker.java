package pascal.netx.compiler.analysis.ast;

import pascal.netx.compiler.ast.ASTListener;
import pascal.netx.compiler.ast.ASTNode;
import pascal.netx.compiler.ast.PackageNode;
import pascal.netx.compiler.ast.expr.IdentifierExpr;

import java.util.Objects;

public final class SourceLocChecker extends ASTAnalysis implements ASTListener {

    public SourceLocChecker(PackageNode design) {
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
