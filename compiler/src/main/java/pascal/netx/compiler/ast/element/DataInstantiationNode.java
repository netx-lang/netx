package pascal.netx.compiler.ast.element;

import pascal.netx.compiler.ast.ASTListener;
import pascal.netx.compiler.ast.ASTNode;
import pascal.netx.compiler.ast.expr.SymbolExpr;

import java.util.List;
import java.util.Objects;

public final class DataInstantiationNode extends ASTNode implements ComponentElement {
    private final List<DataRestNode> dataRests;
    private final Kind kind;
    private SymbolExpr factory;

    private DataInstantiationNode(Kind kind, List<DataRestNode> dataRests, SymbolExpr factory) {
        this.dataRests = dataRests;
        this.kind = kind;
        this.factory = factory;
    }

    public static DataInstantiationNode of(Kind kind, List<DataRestNode> ids, SymbolExpr type) {
        return new DataInstantiationNode(kind, ids, type);
    }

    @Override
    public void walk(ASTListener listener) {
        listener.enter(this);
        dataRests.forEach(symbol -> symbol.walk(listener));
        if (factory != null) {
            factory.walk(listener);
        }
        listener.exit(this);
    }

    public List<DataRestNode> getDataRests() {
        return dataRests;
    }

    public SymbolExpr getFactory() {
        return factory;
    }

    public void setFactory(SymbolExpr factory) {
        this.factory = factory;
    }

    public boolean isAutoFactory() {
        return factory == null;
    }

    public Kind getKind() {
        return kind;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (DataInstantiationNode) obj;
        return Objects.equals(this.dataRests, that.dataRests) &&
                Objects.equals(this.factory, that.factory);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dataRests, factory);
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        switch (kind) {
            case WIRE ->  sb.append("wire ");
            case BUNDLE -> sb.append("bundle ");
        }
        sb.append(dataRests.getFirst());
        for (int i = 1; i < dataRests.size(); i++) {
            sb.append(", ").append(dataRests.get(i));
        }
        sb.append(" of ");
        sb.append(Objects.requireNonNullElse(factory, "auto"));
        return sb.toString();
    }

    public enum Kind {
        WIRE, BUNDLE
    }
}
