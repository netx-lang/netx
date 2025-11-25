package pascal.netx.compiler.circuit.scope.binding;

import pascal.netx.compiler.circuit.Entity;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public record OrderedArgBindList(List<Entity> args) implements ArgBindList {
    public static OrderedArgBindList ofEmpty() {
        return new OrderedArgBindList(new ArrayList<>());
    }

    public void add(Entity entity) {
        args.add(entity);
    }

    @Override
    public List<Entity> getArguments() {
        return args;
    }

    @Override
    public int size() {
        return args.size();
    }

    @Override
    public boolean isEmpty() {
        return args.isEmpty();
    }

    @Override
    public Entity get(String name, int index) {
        return  args.get(index);
    }

    @Override
    @Nonnull
    public String toString() {
        return "(" + String.join(", ", args.stream().map(Entity::toString).toList()) + ")";
    }
}
