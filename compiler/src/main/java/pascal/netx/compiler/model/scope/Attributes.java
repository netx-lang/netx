package pascal.netx.compiler.model.scope;

import pascal.netx.compiler.model.Entity;
import pascal.netx.compiler.model.variable.Variable;
import pascal.netx.util.lang.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.UnaryOperator;

public final class Attributes {
    private final Map<Identifier, Entity> attributes = new HashMap<>();

    public void addAttribute(Identifier name, Entity entity) {
        attributes.put(name, entity);
    }

    public Entity get(Identifier id) {
        return attributes.get(id);
    }

    public Set<Map.Entry<Identifier, Entity>> entrySet() {
        return attributes.entrySet();
    }

    public boolean isEmpty() {
        return attributes.isEmpty();
    }

    public void replaceAllVariable(UnaryOperator<Variable> operator) {
        for (var entry : attributes.entrySet()) {
            if (entry.getValue() instanceof Variable variable) {
                entry.setValue(operator.apply(variable));
            }
        }
    }
}
