package pascal.hardscript.compiler.model;

import pascal.hardscript.compiler.model.data.selector.BitSelector;
import pascal.hardscript.compiler.model.data.selector.PartSelector;
import pascal.hardscript.compiler.model.data.selector.Selector;

import java.util.List;
import java.util.function.Consumer;

public record ListEntity(List<Entity> entities) implements Entity {

    public Entity select(Selector selector) {
        return switch (selector) {
            case BitSelector bitSelector -> entities.get(bitSelector.index().toInt());
            case PartSelector partSelector -> {
                int start = partSelector.start().toInt();
                int end = partSelector.end().toInt();
                yield ListEntity.of(entities.subList(
                        Integer.min(start, end), Integer.max(start, end) + 1));
            }
        };
    }

    public void forEach(Consumer<Entity> consumer) {
        entities.forEach(consumer);
    }

    public static ListEntity of(List<Entity> values) {
        return new ListEntity(values);
    }

    public static ListEntity ofEmpty() {
        return new ListEntity(List.of());
    }
}
