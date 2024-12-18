package pascal.hardscript.compiler.ast.element;

import pascal.hardscript.compiler.ast.ASTListener;
import pascal.hardscript.compiler.ast.ASTNode;
import pascal.hardscript.compiler.ast.expr.ExprNode;
import pascal.hardscript.compiler.ast.expr.SymbolExpr;

import java.util.List;
import java.util.Objects;

public final class DataInstantiationNode extends ASTNode implements ComponentElement {
    private final List<DataRestNode> dataRests;
    private final Kind kind;
    private SymbolExpr type;

    private DataInstantiationNode(Kind kind, List<DataRestNode> dataRests, SymbolExpr type) {
        this.dataRests = dataRests;
        this.kind = kind;
        this.type = type;
    }

    public static DataInstantiationNode of(Kind kind, List<DataRestNode> ids, SymbolExpr type) {
        return new DataInstantiationNode(kind, ids, type);
    }

    @Override
    public void walk(ASTListener listener) {
        listener.enter(this);
        dataRests.forEach(symbol -> symbol.walk(listener));
        if (type != null) {
            type.walk(listener);
        }
        listener.exit(this);
    }

    public List<DataRestNode> getDataRests() {
        return dataRests;
    }

    public ExprNode getType() {
        return type;
    }

    public void setType(SymbolExpr type) {
        this.type = type;
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
                Objects.equals(this.type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dataRests, type);
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        sb.append("wire ");
        sb.append(dataRests.getFirst());
        for (int i = 1; i < dataRests.size(); i++) {
            sb.append(", ").append(dataRests.get(i));
        }
        sb.append(" of ");
        sb.append(Objects.requireNonNullElse(type, "auto"));
        return sb.toString();
    }

    public enum Kind {
        WIRE, BUNDLE
    }
}
