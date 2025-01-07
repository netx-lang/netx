package pascal.netx.compiler.model.expr;

import pascal.netx.util.lang.Identifier;
import pascal.netx.util.lang.SourceLoc;

import java.util.List;

public final class ImportRef extends PrimaryExpr {
    private final List<Identifier> references;
    private final SourceLoc sourceLoc;

    public ImportRef(List<Identifier> references, SourceLoc sourceLoc) {
        this.references = references;
        this.sourceLoc = sourceLoc;
    }

    public static ImportRef of(List<Identifier> references, SourceLoc sourceLoc) {
        return new ImportRef(references, sourceLoc);
    }

    public List<Identifier> getReferences() {
        return references;
    }

    public SourceLoc getSourceLoc() {
        return sourceLoc;
    }
}
