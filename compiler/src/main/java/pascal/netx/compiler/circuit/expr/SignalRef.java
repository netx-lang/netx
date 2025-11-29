package pascal.netx.compiler.circuit.expr;

import pascal.netx.compiler.circuit.signal.Signal;

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
    public String toString() {
        if (hasAttrBind()) {
            return signal.toString() + attrBindList.toString();
        }
        return signal.toString();
    }
}
