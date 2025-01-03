package pascal.netx.compiler.ast;

import pascal.netx.util.lang.Identifier;

import java.util.Objects;

public class PackageInfoNode extends ASTNode {
    private Identifier id;
    private Kind kind;

    private PackageInfoNode(Kind kind, Identifier id) {
        this.id = id;
        this.kind = kind;
    }

    public static PackageInfoNode of(Kind kind, Identifier id) {
        return new PackageInfoNode(kind, id);
    }

    public Identifier getIdentifier() {
        return id;
    }

    public void setId(Identifier id) {
        this.id = id;
    }

    public Kind getKind() {
        return kind;
    }

    public void setKind(Kind kind) {
        this.kind = kind;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PackageInfoNode that = (PackageInfoNode) o;
        return Objects.equals(id, that.id) && kind == that.kind;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, kind);
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        sb.append(kind.name().toLowerCase());
        if (id != null) {
            sb.append(" ").append(id);
        }
        sb.append(";\n");
        return sb.toString();
    }

    @Override
    public void walk(ASTListener listener) {
        listener.enter(this);
        listener.exit(this);
    }

    public enum Kind {
        LIBRARY, APPLICATION
    }
}
