package pascal.hardscript.compiler.ast;

import pascal.hardscript.compiler.ast.component.ComponentNode;

import java.util.List;
import java.util.Objects;

public final class DesignNode extends ASTNode {
    private final List<ComponentNode> components;

    private DesignNode(List<ComponentNode> components) {
        this.components = components;
    }

    public static DesignNode of(List<ComponentNode> modules) {
        return new DesignNode(modules);
    }

    @Override
    public void walk(ASTListener listener) {
        listener.enter(this);
        components.forEach(module -> module.walk(listener));
        listener.exit(this);
    }

    public List<ComponentNode> getComponents() {
        return components;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (DesignNode) obj;
        return Objects.equals(this.components, that.components);
    }

    @Override
    public int hashCode() {
        return Objects.hash(components);
    }

    @Override
    public String toString() {
        return "DesignNode[" +
                "modules=" + components + ']';
    }
}
