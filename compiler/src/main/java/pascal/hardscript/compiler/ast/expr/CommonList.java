package pascal.hardscript.compiler.ast.expr;

import pascal.hardscript.compiler.ast.ASTListener;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public final class CommonList extends ListExpr {
    private final List<ExprNode> elements;

    private CommonList(List<ExprNode> elements) {
        this.elements = new LinkedList<>(elements);
    }

    public static CommonList of(List<ExprNode> elements) {
        return new CommonList(elements);
    }

    public static CommonList ofEmpty() {
        return new CommonList(List.of());
    }

    @Override
    public void walk(ASTListener listener) {
        listener.enter(this);
        elements.forEach(element -> element.walk(listener));
        listener.exit(this);
    }

    public List<ExprNode> getElements() {
        return elements;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (CommonList) obj;
        return Objects.equals(this.elements, that.elements);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elements);
    }

    @Override
    public String toString() {
        return "CommonList[" +
                "elements=" + elements + ']';
    }
}
