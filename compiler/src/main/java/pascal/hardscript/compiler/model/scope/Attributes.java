package pascal.hardscript.compiler.model.scope;

import pascal.hardscript.compiler.model.instance.Instance;
import pascal.hardscript.util.lang.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class Attributes {
    private final Map<Identifier, Instance> attributes = new HashMap<>();

    public void addAttribute(Identifier name, Instance entity) {
        attributes.put(name, entity);
    }

    public Instance get(Identifier id) {
        return attributes.get(id);
    }

    public Set<Map.Entry<Identifier, Instance>> entrySet() {
        return attributes.entrySet();
    }

    public boolean isEmpty() {
        return attributes.isEmpty();
    }
}
