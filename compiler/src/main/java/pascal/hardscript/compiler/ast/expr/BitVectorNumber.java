package pascal.hardscript.compiler.ast.expr;

import pascal.hardscript.compiler.ast.ASTListener;
import pascal.hardscript.util.lang.BitVector;

import java.util.Objects;

public final class BitVectorNumber extends NumberExpr {
    private BitVector representation;

    private BitVectorNumber(BitVector representation) {
        this.representation = representation;
    }

    public static BitVectorNumber of(BitVector rep) {
        return new BitVectorNumber(rep);
    }

    @Override
    public void walk(ASTListener listener) {
        listener.enter(this);
        listener.exit(this);
    }

    public BitVector getRepresentation() {
        return representation;
    }

    public void setRepresentation(BitVector representation) {
        this.representation = representation;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (BitVectorNumber) obj;
        return Objects.equals(this.representation, that.representation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(representation);
    }

    @Override
    public String toString() {
        return "BitVectorNumber[" +
                "rep=" + representation + ']';
    }
}
