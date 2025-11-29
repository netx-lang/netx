package pascal.netx.compiler.circuit.expr;

import pascal.netx.compiler.ast.element.AbstractComponentNode;
import pascal.netx.compiler.ast.element.InterfaceNode;
import pascal.netx.compiler.circuit.scope.binding.AttrBindList;
import pascal.netx.util.exception.ShallNotReachHereError;
import pascal.netx.util.lang.Identifier;
import pascal.netx.util.lang.SourceLoc;

public final class ComponentRef extends PrimaryExpr implements InstantiationRef {
    private final AbstractComponentNode definition;

    private final Identifier identifier;

    public ComponentRef(AbstractComponentNode definition,
                        Identifier identifier, AttrBindList attrBindList) {
        this.definition = definition;
        this.identifier = identifier;
        this.attrBindList = attrBindList;
    }

    public static ComponentRef of(AbstractComponentNode definition, Identifier id) {
        return new ComponentRef(definition, id, AttrBindList.ofEmpty());
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
    public SourceLoc getSourceLoc() {
        return identifier.getSourceLoc();
    }

    @Override
    public String toString() {
        if (hasAttrBind()) {
            return identifier.toString() + attrBindList;
        }
        return identifier.toString();
    }
}
