package pascal.hardscript.compiler.model.component;

import pascal.hardscript.compiler.ast.component.ComponentNode;
import pascal.hardscript.compiler.model.Entity;

import java.util.List;
import java.util.Objects;

public final class ComponentAtom extends AtomType {
    private final ComponentNode definition;

    public ComponentAtom(ComponentNode definition, List<Entity> parameters) {
        this.definition = definition;
        this.parameters = parameters;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComponentAtom that = (ComponentAtom) o;
        return Objects.equals(definition, that.definition);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(definition);
    }

    @Override
    public AtomType copyOf() {
        return ComponentAtom.of(definition, parameters);
    }
}
