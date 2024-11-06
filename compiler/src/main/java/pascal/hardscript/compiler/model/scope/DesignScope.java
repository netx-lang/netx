package pascal.hardscript.compiler.model.scope;

import pascal.hardscript.util.lang.Identifier;

import java.util.Stack;
import java.util.function.Consumer;

public final class DesignScope extends Scope {
    private final ComponentInfo componentInfo;

    DesignScope(Scope parent, ComponentInfo componentInfo) {
        super(parent);
        this.componentInfo = componentInfo;
    }

    public ComponentInfo getComponentInfo() {
        return componentInfo;
    }

    public boolean containsInfo(Identifier identifier) {
        return componentInfo.contains(identifier);
    }

    public static DesignScope newDesign(ComponentInfo componentInfo) {
        return new DesignScope(null, componentInfo);
    }

    public void forEachScope(Consumer<Scope> consumer) {
        Stack<Scope> stack = new Stack<>();
        assert getChildren().size() == 1;
        stack.push(getChildren().getFirst());
        while (!stack.isEmpty()) {
            Scope scope = stack.pop();
            consumer.accept(scope);
            stack.addAll(scope.getChildren());
        }
    }
}
