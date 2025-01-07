package pascal.netx.compiler.model;

import pascal.netx.compiler.model.variable.selector.BitSelector;
import pascal.netx.compiler.model.variable.selector.PartSelector;
import pascal.netx.compiler.model.variable.selector.Selector;

import java.util.List;
import java.util.function.Consumer;

public record EntityList(List<Entity> entities) implements Entity {

    public static EntityList of(List<Entity> values) {
        return new EntityList(values);
    }

    public static EntityList ofEmpty() {
        return new EntityList(List.of());
    }

    public Entity select(Selector selector) {
        return switch (selector) {
            case BitSelector bitSelector -> entities.get(bitSelector.index().toInt());
            case PartSelector partSelector -> {
                int start = partSelector.start().toInt();
                int end = partSelector.end().toInt();
                yield EntityList.of(entities.subList(
                        Integer.min(start, end), Integer.max(start, end) + 1));
            }
        };
    }

    public void forEach(Consumer<Entity> consumer) {
        entities.forEach(consumer);
    }
}
