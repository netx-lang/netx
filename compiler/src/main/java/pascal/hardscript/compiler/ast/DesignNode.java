package pascal.hardscript.compiler.ast;

import java.util.List;

public record DesignNode(List<ModuleNode> modules) implements ASTNode {
    public static DesignNode of(List<ModuleNode> modules) {
        return new DesignNode(modules);
    }
}
