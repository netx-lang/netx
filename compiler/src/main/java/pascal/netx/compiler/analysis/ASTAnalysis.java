package pascal.netx.compiler.analysis;

import pascal.netx.compiler.ast.PackageNode;

public abstract class ASTAnalysis implements Analysis {

    protected final PackageNode design;

    public ASTAnalysis(PackageNode design) {
        this.design = design;
    }
}
