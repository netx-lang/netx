package pascal.netx.compiler.model.instance.type;

import pascal.netx.util.lang.Identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class InterfaceType implements InstanceType {
    private final List<InstanceType> types = new ArrayList<>();
    private final Identifier id;

    private InterfaceType(Identifier id) {
        this.id = id;
    }

    public static InterfaceType of(Identifier id, List<InstanceType> types) {
        var result = new InterfaceType(id);
        result.types.addAll(types);
        return result;
    }

    public List<InstanceType> getTypes() {
        return Collections.unmodifiableList(types);
    }

    public Identifier getId() {
        return id;
    }
}
