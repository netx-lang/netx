package pascal.netx.compiler.circuit;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class EList extends LinkedList<Entity> implements Entity {
    public static EList of(List<Entity> values) {
        var list = new EList();
        list.addAll(values);
        return list;
    }

    public static EList of(Entity... values) {
        var list = new EList();
        list.addAll(Arrays.asList(values));
        return list;
    }

    public static EList ofEmpty() {
        return new EList();
    }
}
