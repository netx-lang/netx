package pascal.hardscript.compiler.ast;

import pascal.hardscript.compiler.ast.module.ModuleNode;

import java.util.List;

public record DesignNode(List<ModuleNode> modules) implements ASTNode {
    public static DesignNode of(List<ModuleNode> modules) {
        return new DesignNode(modules);
    }

    @Override
    public void walk(ASTListener listener) {
        listener.enter(this);
        modules.forEach(module -> module.walk(listener));
        listener.exit(this);
    }
}
