package pascal.netx.compiler.model.circuit.expr;

import pascal.netx.util.exception.ShallNotReachHereError;
import pascal.netx.util.lang.Identifier;
import pascal.netx.util.lang.SourceLoc;

import java.util.List;

public final class ImportRef extends PrimaryExpr implements InstantiationRef {
    private final List<Identifier> references;
    private final SourceLoc sourceLoc;

    private ImportRef(List<Identifier> references, SourceLoc sourceLoc) {
        this.references = references;
        this.sourceLoc = sourceLoc;
    }

    public static ImportRef of(List<Identifier> references) {
        return new ImportRef(references, SourceLoc.none());
    }

    @Override
    public SourceLoc getSourceLoc() {
        return sourceLoc;
    }

    public ImportRef copy(SourceLoc sourceLoc) {
        var result = new ImportRef(references, sourceLoc);
        if (attrBindList != null) {
            result.attrBindList = attrBindList;
        }
        return result;
    }

    @Override
    public String toString() {
        var base = String.join(".", references.stream().map(Identifier::name).toList());
        if (hasAttrBind()) {
            return base + attrBindList;
        }
        return base;
    }

    public Identifier repoRef() {
        return switch (sourceKind()) {
            case REPO -> references.getFirst();
            case PACKAGE -> throw new ShallNotReachHereError();
        };
    }

    public Identifier packageRef() {
        return switch (sourceKind()) {
            case REPO -> references.get(1);
            case PACKAGE -> references.get(0);
        };
    }

    public Identifier componentRef() {
        return switch (sourceKind()) {
            case REPO -> references.get(2);
            case PACKAGE -> references.get(1);
        };
    }

    public SourceKind sourceKind() {
        return switch (references.size()) {
            case 3 -> SourceKind.REPO;
            case 2 -> SourceKind.PACKAGE;
            default -> throw new ShallNotReachHereError();
        };
    }
    public enum SourceKind {
        REPO, PACKAGE

    }
}
