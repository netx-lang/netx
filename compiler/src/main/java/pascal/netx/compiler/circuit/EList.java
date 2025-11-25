package pascal.netx.compiler.circuit;

import java.util.LinkedList;
import java.util.List;

public class EList extends LinkedList<Entity> implements Entity {
    public static EList of(List<Entity> values) {
        return new EList() {{
            addAll(values);
        }};
    }

    public static EList ofEmpty() {
        return new EList();
    }
}
