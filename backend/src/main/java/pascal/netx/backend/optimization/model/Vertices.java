package pascal.netx.backend.optimization.model;

import java.util.function.Predicate;

public final class Vertices {
    public static boolean isCombinational(Vertex u) {
        return u instanceof Component c
                && c.label() != Component.Label.REGISTER
                && c.label() != Component.Label.ASYNC_REGISTER;
    }

    public static Predicate<Component> hasLabel(Component.Label label) {
        return comp -> comp.label() == label;
    }
}
