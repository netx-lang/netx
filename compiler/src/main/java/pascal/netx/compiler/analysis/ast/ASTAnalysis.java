package pascal.netx.compiler.analysis.ast;

import pascal.netx.compiler.analysis.Analysis;
import pascal.netx.compiler.ast.PackageNode;

public abstract class ASTAnalysis implements Analysis {

    protected final PackageNode design;

    public ASTAnalysis(PackageNode design) {
        this.design = design;
    }
}
