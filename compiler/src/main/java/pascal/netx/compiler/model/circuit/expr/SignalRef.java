package pascal.netx.compiler.model.circuit.expr;

import pascal.netx.compiler.model.circuit.signal.Signal;
import pascal.netx.util.lang.SourceLoc;

public final class SignalRef extends PrimaryExpr {
    private final Signal signal;

    private SignalRef(Signal signal) {
        this.signal = signal;
    }

    public static SignalRef of(Signal source) {
        return new SignalRef(source);
    }

    public Signal getInstance() {
        return signal;
    }

    @Override
    public SourceLoc getSourceLoc() {
        return signal.getSourceLoc();
    }

    @Override
    public String toString() {
        if (hasAttrBind()) {
            return signal.toString() + attrBindList.toString();
        }
        return signal.toString();
    }
}
