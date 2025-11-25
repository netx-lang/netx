package pascal.netx.compiler.circuit.scope.binding;

import pascal.netx.compiler.circuit.Entity;

import java.util.List;

public sealed interface ArgBindList permits OrderedArgBindList, MultiBinding {
    static ArgBindList ofEmpty() {
        return OrderedArgBindList.ofEmpty();
    }

    List<Entity> getArguments();

    int size();

    boolean isEmpty();

    Entity get(String name, int index);
}
