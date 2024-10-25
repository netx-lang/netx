package pascal.hardscript.compiler.ast;

import pascal.hardscript.compiler.ast.module.ModuleNode;

import java.util.List;
import java.util.Objects;

public final class DesignNode extends ASTNode {
    private final List<ModuleNode> modules;

    private DesignNode(List<ModuleNode> modules) {
        this.modules = modules;
    }

    public static DesignNode of(List<ModuleNode> modules) {
        return new DesignNode(modules);
    }

    @Override
    public void walk(ASTListener listener) {
        listener.enter(this);
        modules.forEach(module -> module.walk(listener));
        listener.exit(this);
    }

    public List<ModuleNode> getModules() {
        return modules;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (DesignNode) obj;
        return Objects.equals(this.modules, that.modules);
    }

    @Override
    public int hashCode() {
        return Objects.hash(modules);
    }

    @Override
    public String toString() {
        return "DesignNode[" +
                "modules=" + modules + ']';
    }
}
