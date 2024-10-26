package pascal.hardscript.compiler.model.circuit.builtin.type;

import pascal.hardscript.compiler.model.circuit.Component;

public record RegisterType(BitType bitType, TriggerSpec trigger, ResetSpec reset) implements ComponentType {

    public static RegisterType of(BitType bitType, TriggerSpec trigger, ResetSpec reset) {
        return new RegisterType(bitType, trigger, reset);
    }

    public static RegisterType of(BitType bitType, TriggerSpec trigger) {
        return new RegisterType(bitType, trigger, null);
    }

    public record TriggerSpec(Edge edge, Component clock) {

        public static TriggerSpec of(Edge edge, Component clock) {
            return new TriggerSpec(edge, clock);
        }
    }

    public record ResetSpec(Edge edge, Component reset) {

        public static ResetSpec of(Edge edge, Component reset) {
            return new ResetSpec(edge, reset);
        }
    }

    public enum Edge {
        POSITIVE, NEGATIVE
    }
}
