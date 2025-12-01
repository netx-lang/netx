package pascal.netx.compiler.model.circuit.scope.binding;

import pascal.netx.compiler.model.circuit.Entity;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public record OrderedAttrBindList(List<Entity> attrs) implements AttrBindList {
    public static OrderedAttrBindList ofEmpty() {
        return new OrderedAttrBindList(new ArrayList<>());
    }

    public void add(Entity entity) {
        attrs.add(entity);
    }

    @Override
    public List<Entity> getArguments() {
        return attrs;
    }

    @Override
    public int size() {
        return attrs.size();
    }

    @Override
    public boolean isEmpty() {
        return attrs.isEmpty();
    }

    @Override
    public Entity get(String name, int index) {
        return  attrs.get(index);
    }

    @Override
    @Nonnull
    public String toString() {
        return "(" + String.join(", ", attrs.stream().map(Entity::toString).toList()) + ")";
    }
}
