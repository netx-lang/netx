package pascal.netx.compiler.backend.analysis.ast;

import pascal.netx.compiler.backend.analysis.Analysis;
import pascal.netx.compiler.model.ast.PackageNode;

/**
 * Base class for analyses that operate on the abstract syntax tree (AST).
 */
public abstract class ASTAnalysis implements Analysis {
    protected final PackageNode design;

    public ASTAnalysis(PackageNode design) {
        this.design = design;
    }
}
