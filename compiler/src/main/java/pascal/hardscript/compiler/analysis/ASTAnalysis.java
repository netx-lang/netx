package pascal.hardscript.compiler.analysis;

import pascal.hardscript.compiler.ast.DesignNode;

public abstract class ASTAnalysis {

    protected final DesignNode design;

    public ASTAnalysis(DesignNode design) {
        this.design = design;
    }

    abstract public void analyze();
}
