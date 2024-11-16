package pascal.hardscript.compiler.model.component;

import pascal.hardscript.compiler.ast.component.ComponentNode;
import pascal.hardscript.compiler.model.Entity;
import pascal.hardscript.util.lang.Identifier;

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

    public static ComponentAtom of(ComponentNode definition, Identifier identifier, List<Entity> parameters) {
        return new ComponentAtom(definition, identifier, parameters);
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
