package pascal.hardscript.compiler.ast;

import pascal.hardscript.util.lang.SourceLoc;

public abstract class ASTNode {

    protected SourceLoc sourceLoc;

    public SourceLoc getSourceLoc() {
        return sourceLoc;
    }

    @SuppressWarnings("unchecked")
    public <T extends ASTNode> T setSourceLoc(SourceLoc sourceLoc) {
        this.sourceLoc = sourceLoc;
        return (T) this;
    }

    abstract public void walk(ASTListener listener);
}
