package pascal.hardscript.compiler.model.component;

import pascal.hardscript.compiler.model.Entity;

import java.util.List;

public sealed abstract class AtomType implements ComponentType permits BuiltinAtom, ComponentAtom {
    protected List<Entity> parameters;

    public List<Entity> getParameters() {
        return parameters;
    }

    abstract public AtomType copyOf();

    public AtomType parameterize(List<Entity> parameters) {
        var result = this.copyOf();
        result.parameters = parameters;
        return result;
    }
}
