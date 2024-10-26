package pascal.hardscript.compiler.ast.module.type;


import pascal.hardscript.compiler.ast.ASTListener;
import pascal.hardscript.compiler.ast.expr.ExprNode;

import java.util.Objects;

public final class RegType extends ComponentTypeNode {
    private BitType bitType;
    private TriggerSpec trigger;
    private ResetSpec reset;

    private RegType(BitType bitType, TriggerSpec trigger, ResetSpec reset) {
        this.bitType = bitType;
        this.trigger = trigger;
        this.reset = reset;
    }

    public static RegType of(BitType bitType, TriggerSpec trigger) {
        return new RegType(bitType, trigger, null);
    }

    public static RegType of(BitType bitType, TriggerSpec trigger, ResetSpec reset) {
        return new RegType(bitType, trigger, reset);
    }

    public BitType getBitType() {
        return bitType;
    }

    public void setBitType(BitType bitType) {
        this.bitType = bitType;
    }

    public TriggerSpec getTrigger() {
        return trigger;
    }

    public void setTrigger(TriggerSpec trigger) {
        this.trigger = trigger;
    }

    public ResetSpec getReset() {
        return reset;
    }

    public void setReset(ResetSpec reset) {
        this.reset = reset;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (RegType) obj;
        return Objects.equals(this.bitType, that.bitType) &&
                Objects.equals(this.trigger, that.trigger) &&
                Objects.equals(this.reset, that.reset);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bitType, trigger, reset);
    }

    @Override
    public String toString() {
        return "RegType[" +
                "bitType=" + bitType + ", " +
                "trigger=" + trigger + ", " +
                "reset=" + reset + ']';
    }

    @Override
    public void walk(ASTListener listener) {
        listener.enter(this);
        bitType.walk(listener);
        trigger.clock().walk(listener);
        if (reset != null) {
            reset.reset().walk(listener);
        }
        listener.exit(this);
    }


    public record TriggerSpec(Edge edge, ExprNode clock) {
        public static TriggerSpec of(Edge edge, ExprNode clock) {
            return new TriggerSpec(edge, clock);
        }
    }

    public record ResetSpec(Edge edge, ExprNode reset) {
        public static ResetSpec of(Edge edge, ExprNode reset) {
            return new ResetSpec(edge, reset);
        }
    }

    public enum Edge {
        POSITIVE, NEGATIVE
    }
}
