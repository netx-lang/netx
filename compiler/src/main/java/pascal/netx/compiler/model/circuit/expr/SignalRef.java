package pascal.netx.compiler.model.circuit.expr;

import pascal.netx.compiler.model.circuit.signal.Signal;
import pascal.netx.util.lang.SourceLoc;

public final class SignalRef extends PrimaryExpr {
    private final Signal signal;
    private final SourceLoc refLoc;

    private SignalRef(Signal signal, SourceLoc refLoc) {
        this.signal = signal;
        this.refLoc = refLoc;
    }

    public static SignalRef of(Signal source, SourceLoc refLoc) {
        return new SignalRef(source, refLoc);
    }

    public Signal getInstance() {
        return signal;
    }

    @Override
    public SourceLoc getSourceLoc() {
        return refLoc;
    }

    @Override
    public String toString() {
        if (hasAttrBind()) {
            return signal.toString() + attrBindList.toString();
        }
        return signal.toString();
    }
}
