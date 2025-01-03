package pascal.netx.compiler.analysis;

import pascal.netx.compiler.ast.DesignNode;

public abstract class ASTAnalysis implements Analysis {

    protected final DesignNode design;

    public ASTAnalysis(DesignNode design) {
        this.design = design;
    }
}
