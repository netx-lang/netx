package pascal.netx.compiler.model.component;

import pascal.netx.compiler.ast.element.ComponentNode;
import pascal.netx.compiler.model.Entity;
import pascal.netx.util.lang.Identifier;

import java.util.List;

public final class ComponentAtom extends AtomType {
    private final ComponentNode definition;
    private Identifier identifier;

    public ComponentAtom(ComponentNode definition, Identifier identifier, List<Entity> parameters) {
        this.definition = definition;
        this.identifier = identifier;
        this.parameters = parameters;
    }

    public static ComponentAtom of(ComponentNode definition, Identifier id) {
        return new ComponentAtom(definition, id, List.of());
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public void setIdentifier(Identifier identifier) {
        this.identifier = identifier;
    }

    public ComponentNode getDefinition() {
        return definition;
    }
}
