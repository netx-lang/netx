package pascal.hardscript.compiler.model.scope;

import java.util.Stack;
import java.util.function.Consumer;

public final class Design {
    private ComponentScope top;

    Design() {
    }

    public static Design newDesign() {
        return new Design();
    }

    public void setTop(ComponentScope top) {
        this.top = top;
    }

    public ComponentScope getTop() {
        return top;
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
