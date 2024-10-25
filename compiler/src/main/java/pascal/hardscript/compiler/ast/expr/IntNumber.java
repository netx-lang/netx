package pascal.hardscript.compiler.ast.expr;

import pascal.hardscript.compiler.ast.ASTListener;

public record IntNumber(int value) implements NumberExpr {

    public static IntNumber of(int value) {
        return new IntNumber(value);
    }

    @Override
    public void walk(ASTListener listener) {
        listener.enter(this);
        listener.exit(this);
    }
}
