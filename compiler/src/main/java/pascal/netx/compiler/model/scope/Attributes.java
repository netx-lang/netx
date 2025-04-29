package pascal.netx.compiler.model.scope;

import pascal.netx.compiler.ast.element.AbstractComponentNode;
import pascal.netx.compiler.model.Entity;
import pascal.netx.compiler.model.scope.binding.ArgBindList;
import pascal.netx.compiler.model.scope.binding.MultiBinding;
import pascal.netx.compiler.model.scope.binding.OrderedArgBindList;
import pascal.netx.compiler.model.variable.Variable;
import pascal.netx.util.lang.Identifier;
import pascal.netx.util.lang.SourceLoc;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.UnaryOperator;

import static pascal.netx.compiler.model.Utils.requiresMatchedSize;

public final class Attributes {
    private final Map<Identifier, Entity> attributes = new HashMap<>();

    public static Attributes from(SourceLoc sourceLoc, AbstractComponentNode node, ArgBindList arguments) {
        Attributes attributes = new Attributes();
        switch (arguments) {
            case OrderedArgBindList(var args) -> {
                int size = requiresMatchedSize(sourceLoc, node.getAttributes(), args);
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
}
