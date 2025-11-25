package pascal.netx.compiler.circuit.signal.type;

import pascal.netx.util.lang.Identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class InterfaceType implements Type {
    private final List<Type> types = new ArrayList<>();
    private final Identifier id;

    private InterfaceType(Identifier id) {
        this.id = id;
    }

    public static InterfaceType of(Identifier id, List<Type> types) {
        var result = new InterfaceType(id);
        result.types.addAll(types);
        return result;
    }

    public List<Type> getTypes() {
        return Collections.unmodifiableList(types);
    }

    public Identifier getIdentifier() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        InterfaceType that = (InterfaceType) o;
        return Objects.equals(types, that.types) && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(types, id);
    }
}
