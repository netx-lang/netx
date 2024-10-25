package pascal.hardscript.compiler.ast.expr;

import pascal.hardscript.compiler.ast.ASTListener;
import pascal.hardscript.util.lang.BitVector;

public record BitVectorNumber(BitVector rep) implements NumberExpr {

    public static BitVectorNumber of(BitVector rep) {
        return new BitVectorNumber(rep);
    }

    @Override
    public void walk(ASTListener listener) {
        listener.enter(this);
        listener.exit(this);
    }
}
