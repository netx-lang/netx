package pascal.hardscript.compiler.model.component;

import pascal.hardscript.compiler.ast.component.ComponentNode;
import pascal.hardscript.compiler.model.Entity;

import java.util.List;

public final class ComponentAtom extends AtomType {
    private final ComponentNode definition;

    public ComponentAtom(ComponentNode definition, List<Entity> parameters) {
        this.definition = definition;
        setParameters(parameters);
    }

    public static ComponentAtom of(ComponentNode definition) {
        return new ComponentAtom(definition, List.of());
    }

    public static ComponentAtom of(ComponentNode definition, List<Entity> parameters) {
        return new ComponentAtom(definition, parameters);
    }

    public ComponentNode getDefinition() {
        return definition;
    }
}
