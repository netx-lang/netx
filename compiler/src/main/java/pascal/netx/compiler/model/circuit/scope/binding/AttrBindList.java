package pascal.netx.compiler.model.circuit.scope.binding;

import pascal.netx.compiler.model.circuit.Entity;

import java.util.List;

public sealed interface AttrBindList permits OrderedAttrBindList, MultiBinding {
    static AttrBindList ofEmpty() {
        return OrderedAttrBindList.ofEmpty();
    }

    List<Entity> getArguments();

    int size();

    boolean isEmpty();

    Entity get(String name, int index);
}
