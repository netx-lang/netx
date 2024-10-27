package pascal.hardscript.compiler.model.instantiation;

import pascal.hardscript.compiler.model.ParameteriezedEntity;

public record AtomType(ParameteriezedEntity module) implements InstanceType {

    public static AtomType from(ParameteriezedEntity module) {
        return new AtomType(module);
    }
}
