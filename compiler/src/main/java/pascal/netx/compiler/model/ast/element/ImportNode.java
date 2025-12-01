package pascal.netx.compiler.model.ast.element;

import pascal.netx.compiler.model.ast.ASTListener;
import pascal.netx.compiler.model.ast.ASTNode;
import pascal.netx.util.lang.Identifier;

import java.util.List;
import java.util.Objects;

public final class ImportNode extends ASTNode implements PackageElement {
    private List<Identifier> reference;
    private Identifier alias;
    private boolean isWildcard;

    private ImportNode(List<Identifier> reference, Identifier alias, boolean isWildcard) {
        this.reference = reference;
        this.alias = alias;
        this.isWildcard = isWildcard;
    }

    public static ImportNode of(List<Identifier> reference, Identifier alias) {
        return new ImportNode(reference, alias, false);
    }

    public static ImportNode of(List<Identifier> reference) {
        return new ImportNode(reference, null, false);
    }

    public static ImportNode ofWildcard(List<Identifier> reference) {
        return new ImportNode(reference, null, true);
    }

    public List<Identifier> getReference() {
        return reference;
    }

    public void setReference(List<Identifier> reference) {
        this.reference = reference;
    }

    public Identifier getAlias() {
        return alias;
    }

    public boolean hasAlias() {
        return alias != null;
    }

    public void setAlias(Identifier alias) {
        this.alias = alias;
    }

    public boolean isWildcard() {
        return isWildcard;
    }

    public void setWildcard(boolean wildcard) {
        isWildcard = wildcard;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ImportNode that = (ImportNode) o;
        return isWildcard == that.isWildcard && Objects.equals(reference, that.reference) && Objects.equals(alias, that.alias);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reference, alias, isWildcard);
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        sb.append("import ");
        sb.append(String.join(".", reference.stream().map(Identifier::name).toList()));
        if (isWildcard) {
            sb.append(".*");
        } else if (alias != null) {
            sb.append(" as ").append(alias.name());
        }
        sb.append(";");
        return sb.toString();
    }

    @Override
    public void walk(ASTListener listener) {
        listener.enter(this);
        listener.exit(this);
    }
}
