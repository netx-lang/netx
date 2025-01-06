package pascal.netx.compiler.model.expr;

import pascal.netx.compiler.model.Entity;

import java.util.List;

public sealed abstract class AtomExpr implements Expr permits BuiltinAtom, ComponentAtom, ImportedAtom, InstanceAtom {
    protected List<Entity> parameters;

    public List<Entity> getParameters() {
        return parameters;
    }

    public void setParameters(List<Entity> parameters) {
        this.parameters = parameters;
    }
}
