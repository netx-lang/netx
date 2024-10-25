package pascal.hardscript.compiler.ast;

public sealed abstract class Symbol permits Identifier, Interpolated {
    private SourceLoc sourceLoc;

    public SourceLoc getSourceLoc() {
        return sourceLoc;
    }

    public void setSourceLoc(SourceLoc sourceLoc) {
        this.sourceLoc = sourceLoc;
    }
}
