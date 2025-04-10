package pascal.netx.compiler.model.expr;

import pascal.netx.compiler.ast.element.AbstractComponentNode;
import pascal.netx.compiler.ast.element.InterfaceNode;
import pascal.netx.compiler.model.scope.binding.ArgBindList;
import pascal.netx.util.exception.ShallNotReachHereError;
import pascal.netx.util.lang.Identifier;

public final class ComponentRef extends PrimaryExpr {
    private final AbstractComponentNode definition;

    private final Identifier identifier;

    public ComponentRef(AbstractComponentNode definition,
                        Identifier identifier, ArgBindList argBindList) {
        this.definition = definition;
        this.identifier = identifier;
        this.argBindList = argBindList;
    }

    public static ComponentRef of(AbstractComponentNode definition, Identifier id) {
        return new ComponentRef(definition, id, ArgBindList.ofEmpty());
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public AbstractComponentNode getDefinition() {
        if (definition == null) {
            throw new ShallNotReachHereError();
        }
        return definition;
    }

    public boolean isInterface() {
        return definition instanceof InterfaceNode;
    }

    @Override
    public String toString() {
        if (argBindList != null) {
            return identifier.toString() + argBindList;
        }
        return identifier.toString();
    }
}
