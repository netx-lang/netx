package pascal.hardscript.compiler.model.component;

import pascal.hardscript.compiler.model.Entity;

import java.util.List;

public sealed abstract class AtomType implements ComponentType permits BuiltinAtom, ComponentAtom {
    protected List<Entity> parameters;

    public List<Entity> getParameters() {
        return parameters;
    }

    public void setParameters(List<Entity> parameters) {
        this.parameters = parameters;
    }
}
