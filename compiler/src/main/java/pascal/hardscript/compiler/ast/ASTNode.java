package pascal.hardscript.compiler.ast;

public abstract class ASTNode {

    protected SourceLoc sourceLoc;

    public SourceLoc getSourceLoc() {
        return sourceLoc;
    }

    public void setSourceLoc(SourceLoc sourceLoc) {
        this.sourceLoc = sourceLoc;
    }

    abstract public void walk(ASTListener listener);
}
