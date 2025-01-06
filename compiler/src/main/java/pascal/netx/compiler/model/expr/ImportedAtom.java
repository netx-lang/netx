package pascal.netx.compiler.model.expr;

import pascal.netx.util.lang.Identifier;
import pascal.netx.util.lang.SourceLoc;

import java.util.List;

public final class ImportedAtom extends AtomExpr {
    private final List<Identifier> references;
    private final SourceLoc sourceLoc;

    public ImportedAtom(List<Identifier> references, SourceLoc sourceLoc) {
        this.references = references;
        this.sourceLoc = sourceLoc;
    }

    public static ImportedAtom of(List<Identifier> references, SourceLoc sourceLoc) {
        return new ImportedAtom(references, sourceLoc);
    }

    public List<Identifier> getReferences() {
        return references;
    }

    public SourceLoc getSourceLoc() {
        return sourceLoc;
    }
}
