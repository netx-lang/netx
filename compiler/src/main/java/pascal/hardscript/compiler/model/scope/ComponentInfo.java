package pascal.hardscript.compiler.model.scope;

import pascal.hardscript.compiler.ast.component.ComponentNode;
import pascal.hardscript.util.lang.BuiltinComponent;
import pascal.hardscript.util.lang.Identifier;

import java.util.*;

public final class ComponentInfo {
    private final Set<Identifier> components = new HashSet<>();
    private final Set<Identifier> builtin = new HashSet<>();
    private final Map<Identifier, ComponentNode> componentNodes = new HashMap<>();

    public ComponentInfo(List<ComponentNode> components) {
        for (var node : components) {
            this.components.add(node.getIdentifier());
            this.componentNodes.put(node.getIdentifier(), node);
        }
        for (var component : BuiltinComponent.values()) {
            this.builtin.add(Identifier.of(component.name()));
        }
    }

    public boolean contains(Identifier identifier) {
        return containsComponent(identifier) || containsBuiltin(identifier);
    }

    public boolean containsComponent(Identifier identifier) {
        return components.contains(identifier);
    }

    public boolean containsBuiltin(Identifier identifier) {
        return builtin.contains(identifier);
    }

    public ComponentNode getComponentNode(Identifier identifier) {
        return componentNodes.get(identifier);
    }

    public static ComponentInfo of(List<ComponentNode> modules) {
        return new ComponentInfo(modules);
    }
}
