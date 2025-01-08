package pascal.netx.compiler.ast.element;

import pascal.netx.compiler.ast.ASTListener;
import pascal.netx.compiler.ast.ASTNode;
import pascal.netx.util.lang.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class EnumNode extends ASTNode implements PackageElement {
    private final List<Identifier> fields = new ArrayList<>();
    private Identifier name;

    private EnumNode(Identifier name) {
        this.name = name;
    }

    public static EnumNode of(Identifier name) {
        return new EnumNode(name);
    }

    public Identifier getName() {
        return name;
    }

    public void setName(Identifier name) {
        this.name = name;
    }

    public List<Identifier> getFields() {
        return fields;
    }

    public void addField(Identifier field) {
        fields.add(field);
    }

    @Override
    public void walk(ASTListener listener) {
        listener.enter(this);
        listener.exit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        EnumNode enumNode = (EnumNode) o;
        return Objects.equals(name, enumNode.name) && Objects.equals(fields, enumNode.fields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, fields);
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        sb.append("enum ").append(name).append(" {");
        var iterator = fields.iterator();
        sb.append(iterator.next());
        while (iterator.hasNext()) {
            sb.append(", \n").append(iterator.next());
        }
        sb.append("}");
        return sb.toString();
    }
}
