package pascal.hardscript.compiler.model.scope;

import pascal.hardscript.compiler.ast.component.ComponentNode;
import pascal.hardscript.util.lang.Identifier;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Stack;
import java.util.function.Consumer;

public final class DesignScope extends Scope {
    private final ComponentInfo info;

    DesignScope(Scope parent, ComponentInfo info) {
        super(parent);
        this.info = info;
    }

    public static DesignScope newDesign(ComponentInfo info) {
        return new DesignScope(null, info);
    }

    public boolean hasDefinition(Identifier identifier) {
        return info.getComponentNode(identifier) != null;
    }

    @Nonnull
    public ComponentNode getDefinition(Identifier identifier) {
        return Objects.requireNonNull(info.getComponentNode(identifier));
    }

    public ComponentScope getTop() {
        assert getChildren().size() == 1;
        return (ComponentScope) getChildren().getFirst();
    }

    public void forEachScope(Consumer<Scope> consumer) {
        Stack<Scope> stack = new Stack<>();
        stack.push(getTop());
        while (!stack.isEmpty()) {
            Scope scope = stack.pop();
            consumer.accept(scope);
            stack.addAll(scope.getChildren());
        }
    }
}
