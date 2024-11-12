package pascal.hardscript.compiler.model.scope;

import pascal.hardscript.compiler.ast.component.ComponentNode;
import pascal.hardscript.util.lang.Identifier;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class ComponentInfo {
    private final Map<Identifier, ComponentNode> componentNodes = new HashMap<>();

    public ComponentInfo(List<ComponentNode> components) {
        for (var node : components) {
            this.componentNodes.put(node.getIdentifier(), node);
        }
    }

    public boolean hasDefinition(Identifier identifier) {
        return getComponentNode(identifier) != null;
    }

    @Nonnull
    public ComponentNode getDefinition(Identifier identifier) {
        return Objects.requireNonNull(getComponentNode(identifier));
    }

    public ComponentNode getComponentNode(Identifier identifier) {
        return componentNodes.get(identifier);
    }

    public static ComponentInfo of(List<ComponentNode> modules) {
        return new ComponentInfo(modules);
    }
}
