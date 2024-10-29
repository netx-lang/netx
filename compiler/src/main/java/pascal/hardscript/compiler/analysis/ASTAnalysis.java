package pascal.hardscript.compiler.analysis;

import pascal.hardscript.compiler.ast.DesignNode;

public abstract class ASTAnalysis<T> implements Analysis<T> {

    protected final DesignNode design;

    public ASTAnalysis(DesignNode design) {
        this.design = design;
    }
}
