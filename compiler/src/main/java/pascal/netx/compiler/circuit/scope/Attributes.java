package pascal.netx.compiler.circuit.scope;

import pascal.netx.compiler.ast.element.AbstractComponentNode;
import pascal.netx.compiler.circuit.Entity;
import pascal.netx.compiler.circuit.scope.binding.AttrBindList;
import pascal.netx.compiler.circuit.scope.binding.MultiBinding;
import pascal.netx.compiler.circuit.scope.binding.OrderedAttrBindList;
import pascal.netx.compiler.circuit.variable.Variable;
import pascal.netx.util.lang.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.UnaryOperator;

import static pascal.netx.compiler.circuit.Utils.requireEqSize;

public final class Attributes {
    private final Map<Identifier, Entity> attributes = new HashMap<>();

    public static Attributes ofEmpty() {
        return new Attributes();
    }

    public static Attributes from(AbstractComponentNode node, AttrBindList arguments) {
        Attributes attributes = new Attributes();
        switch (arguments) {
            case OrderedAttrBindList(var args) -> {
                // FIXME: message
                int size = requireEqSize(node.getAttributes(), args, "");
                for (int i = 0; i < size; i++) {
                    attributes.addAttribute(node.getAttributes().get(i), args.get(i));
                }
            }
            case MultiBinding binding -> binding.forEach(
                    entry -> attributes.addAttribute(entry.getKey(), entry.getValue()));
        }
        return attributes;
    }

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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Attributes that = (Attributes) o;
        return Objects.equals(attributes, that.attributes);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(attributes);
    }
}
