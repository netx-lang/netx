package pascal.netx.compiler.model.expr;

import pascal.netx.compiler.ast.element.ComponentNode;
import pascal.netx.compiler.model.scope.binding.ArgBindList;
import pascal.netx.util.lang.Identifier;

public final class ComponentRef extends PrimaryExpr {
    private final ComponentNode definition;
    private final Identifier identifier;

    public ComponentRef(ComponentNode definition, Identifier identifier, ArgBindList argBindList) {
        this.definition = definition;
        this.identifier = identifier;
        this.argBindList = argBindList;
    }

    public static ComponentRef of(ComponentNode definition, Identifier id) {
        return new ComponentRef(definition, id, ArgBindList.ofEmpty());
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public ComponentNode getDefinition() {
        return definition;
    }
}
