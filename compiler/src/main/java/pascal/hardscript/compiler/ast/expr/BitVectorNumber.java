package pascal.hardscript.compiler.ast.expr;

import pascal.hardscript.util.lang.BitVector;

public record BitVectorNumber(BitVector rep, boolean isSigned) implements NumberExpr {

    public static BitVectorNumber of(BitVector rep, boolean isSigned) {
        return new BitVectorNumber(rep, isSigned);
    }
}
