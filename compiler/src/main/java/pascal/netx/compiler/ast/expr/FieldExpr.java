package pascal.netx.compiler.ast.expr;

import pascal.netx.compiler.ast.ASTListener;
import pascal.netx.util.lang.Identifier;

import java.util.Objects;

public final class FieldExpr extends ExprNode {
    private ExprNode base;
    private Identifier field;

    private FieldExpr(ExprNode base, Identifier field) {
        this.base = base;
        this.field = field;
    }

    public static FieldExpr of(ExprNode base, Identifier field) {
        return new FieldExpr(base, field);
    }

    public ExprNode getBase() {
        return base;
    }

    public void setBase(ExprNode base) {
        this.base = base;
    }

    public Identifier getField() {
        return field;
    }

    public void setField(Identifier field) {
        this.field = field;
    }

    @Override
    public void walk(ASTListener listener) {
        listener.enter(this);
        base.walk(listener);
        listener.exit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        FieldExpr fieldExpr = (FieldExpr) o;
        return Objects.equals(base, fieldExpr.base) && Objects.equals(field, fieldExpr.field);
    }

    @Override
    public int hashCode() {
        return Objects.hash(base, field);
    }

    @Override
    public String toString() {
        return String.format("%s.%s", base, field);
    }
}
