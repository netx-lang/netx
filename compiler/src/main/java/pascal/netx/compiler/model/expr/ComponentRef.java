package pascal.netx.compiler.model.expr;

import pascal.netx.compiler.ast.element.ComponentNode;
import pascal.netx.compiler.model.Entity;
import pascal.netx.util.lang.Identifier;

import java.util.List;

public final class ComponentRef extends PrimaryExpr {
    private final ComponentNode definition;
    private final Identifier identifier;

    public ComponentRef(ComponentNode definition, Identifier identifier, List<Entity> parameters) {
        this.definition = definition;
        this.identifier = identifier;
        this.parameters = parameters;
    }

    public static ComponentRef of(ComponentNode definition, Identifier id) {
        return new ComponentRef(definition, id, List.of());
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public ComponentNode getDefinition() {
        return definition;
    }
}
