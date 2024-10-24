package pascal.hardscript.compiler.ast.expr.atom;

import pascal.hardscript.util.lang.BitVector;

public record BitVectorNumber(BitVector rep) implements NumberExpr {

    public static BitVectorNumber of(BitVector rep) {
        return new BitVectorNumber(rep);
    }
}
