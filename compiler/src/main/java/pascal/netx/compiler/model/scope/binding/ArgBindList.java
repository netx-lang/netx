package pascal.netx.compiler.model.scope.binding;

import pascal.netx.compiler.model.Entity;

import java.util.List;

public sealed interface ArgBindList permits OrderedArgBindList, MultiBinding {
    static ArgBindList ofEmpty() {
        return OrderedArgBindList.ofEmpty();
    }

    List<Entity> getArguments();

    int size();
}
