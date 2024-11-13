package pascal.hardscript.compiler.ast;

import pascal.hardscript.compiler.ast.component.BindingNode;
import pascal.hardscript.compiler.ast.component.ComponentNode;

import java.util.List;
import java.util.Objects;

public final class DesignNode extends ASTNode {
    private final List<BindingNode> bindings;
    private final List<ComponentNode> components;

    private DesignNode(List<BindingNode> bindings, List<ComponentNode> components) {
        this.bindings = bindings;
        this.components = components;
    }

    public static DesignNode of(List<BindingNode> bindings, List<ComponentNode> modules) {
        return new DesignNode(bindings, modules);
    }

    @Override
    public void walk(ASTListener listener) {
        listener.enter(this);
        bindings.forEach(binding -> binding.walk(listener));
        components.forEach(module -> module.walk(listener));
        listener.exit(this);
    }

    public List<BindingNode> getBindings() {
        return bindings;
    }

    public List<ComponentNode> getComponents() {
        return components;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (DesignNode) obj;
        return Objects.equals(this.bindings, that.bindings) && Objects.equals(this.components, that.components);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bindings, components);
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        for (var binding : bindings) {
            sb.append(binding).append('\n');
        }
        for (var component : components) {
            sb.append(component).append('\n');
        }
        return sb.toString();
    }
}
