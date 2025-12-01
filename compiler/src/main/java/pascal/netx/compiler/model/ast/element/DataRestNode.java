package pascal.netx.compiler.model.ast.element;

import pascal.netx.compiler.model.ast.ASTListener;
import pascal.netx.compiler.model.ast.ASTNode;
import pascal.netx.compiler.model.ast.expr.ExprNode;
import pascal.netx.util.lang.Identifier;

import java.util.List;
import java.util.Objects;

public final class DataRestNode extends ASTNode {
    private Identifier identifier;
    private List<ExprNode> dimensions;

    private DataRestNode(Identifier identifier, List<ExprNode> dimensions) {
        this.identifier = identifier;
        this.dimensions = dimensions;
    }

    public static DataRestNode of(Identifier identifier, List<ExprNode> dimensions) {
        return new DataRestNode(identifier, dimensions);
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public void setIdentifier(Identifier identifier) {
        this.identifier = identifier;
    }

    public List<ExprNode> getDimensions() {
        return dimensions;
    }

    public void setDimension(List<ExprNode> dimensions) {
        this.dimensions = dimensions;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DataRestNode dataRestNode = (DataRestNode) o;
        return Objects.equals(identifier, dataRestNode.identifier) && Objects.equals(dimensions, dataRestNode.dimensions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier, dimensions);
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        sb.append(identifier);
        for (var dimension : dimensions) {
            sb.append("[").append(dimension).append("]");
        }
        return sb.toString();
    }

    @Override
    public void walk(ASTListener listener) {
        listener.enter(this);
        dimensions.forEach(dimension -> dimension.walk(listener));
        listener.exit(this);
    }
}
