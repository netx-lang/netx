package pascal.hardscript.compiler.ast.expr;

import pascal.hardscript.compiler.ast.ASTListener;

import java.util.Objects;

public final class FieldExpr extends ExprNode {
    private ExprNode base;
    private PrimarySymbol field;

    private FieldExpr(ExprNode base, PrimarySymbol field) {
        this.base = base;
        this.field = field;
    }

    public static FieldExpr of(ExprNode base, PrimarySymbol field) {
        return new FieldExpr(base, field);
    }

    public ExprNode getBase() {
        return base;
    }

    public void setBase(ExprNode base) {
        this.base = base;
    }

    public PrimarySymbol getField() {
        return field;
    }

    public void setField(PrimarySymbol field) {
        this.field = field;
    }

    @Override
    public void walk(ASTListener listener) {
        listener.enter(this);
        base.walk(listener);
        field.walk(listener);
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
