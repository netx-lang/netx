package pascal.netx.compiler.backend.analysis.ast;

import pascal.netx.compiler.model.ast.ASTListener;
import pascal.netx.compiler.model.ast.ASTNode;
import pascal.netx.compiler.model.ast.PackageNode;
import pascal.netx.compiler.model.ast.expr.IdentifierExpr;

import java.util.Objects;

/**
 * Checks that all AST nodes have a source location.
 * This is useful for debugging and error reporting.
 */
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
