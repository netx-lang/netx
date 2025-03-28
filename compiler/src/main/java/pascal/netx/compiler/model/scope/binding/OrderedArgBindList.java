package pascal.netx.compiler.model.scope.binding;

import pascal.netx.compiler.model.Entity;

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
}
