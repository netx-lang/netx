package pascal.hardscript.compiler.model.scope;

import pascal.hardscript.compiler.ast.Identifier;

import java.util.Stack;
import java.util.function.Consumer;

public class DesignScope extends Scope {
    private final ModuleInfo moduleInfo;

    DesignScope(Scope parent, ModuleInfo moduleInfo) {
        super(parent);
        this.moduleInfo = moduleInfo;
    }

    public boolean containsInfo(Identifier identifier) {
        return moduleInfo.contains(identifier);
    }

    public static DesignScope newDesign(ModuleInfo moduleInfo) {
        return new DesignScope(null, moduleInfo);
    }

    public void forEachScope(Consumer<Scope> consumer) {
        Stack<Scope> stack = new Stack<>();
        stack.push(this);
        while (!stack.isEmpty()) {
            Scope scope = stack.pop();
            consumer.accept(scope);
            stack.addAll(scope.getChildren());
        }
    }
}
