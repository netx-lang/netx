package pascal.hardscript.compiler.model.scope;

import pascal.hardscript.compiler.ast.component.ComponentNode;
import pascal.hardscript.util.lang.BuiltinComponent;
import pascal.hardscript.util.lang.Identifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ComponentInfo {
    private final Map<Identifier, ComponentNode> componentNodes = new HashMap<>();

    public ComponentInfo(List<ComponentNode> components) {
        for (var node : components) {
            this.componentNodes.put(node.getIdentifier(), node);
        }
    }

    public boolean contains(Identifier identifier) {
        if (containsComponent(identifier)) return true;
        return BuiltinComponent.isBuiltin(identifier);
    }

    public boolean containsComponent(Identifier identifier) {
        return componentNodes.containsKey(identifier);
    }

    public ComponentNode getComponentNode(Identifier identifier) {
        return componentNodes.get(identifier);
    }

    public static ComponentInfo of(List<ComponentNode> modules) {
        return new ComponentInfo(modules);
    }
}
