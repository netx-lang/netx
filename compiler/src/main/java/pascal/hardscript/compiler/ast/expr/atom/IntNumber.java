package pascal.hardscript.compiler.ast.expr.atom;

public record IntNumber(int value) implements NumberExpr {

    public static IntNumber of(int value) {
        return new IntNumber(value);
    }
}
