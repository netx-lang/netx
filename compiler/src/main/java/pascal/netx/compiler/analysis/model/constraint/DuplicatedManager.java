package pascal.netx.compiler.analysis.model.constraint;

import pascal.netx.compiler.model.Entity;
import pascal.netx.compiler.model.scope.ComponentScope;
import pascal.netx.compiler.model.variable.Variable;
import pascal.netx.util.collection.UnionFind;
import pascal.netx.util.lang.BuiltinComponent;
import pascal.netx.util.lang.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static pascal.netx.util.lang.BuiltinComponent.TO;

public class DuplicatedManager {
    private final UnionFind<Variable> set = new UnionFind<>();

    public void collectDuplicatedComponents(ComponentScope scope) {
        Map<Identifier, List<ComponentScope>> idGroups = scope.getChildren().stream()
                .map(ComponentScope.class::cast)
                .collect(Collectors.groupingBy(ComponentScope::getIdentifier));

        List<ComponentScope> toRemove = collectDuplicatedTo(scope);
        for (var entry : idGroups.entrySet()) {

            var idGroup = entry.getValue();
            for (int i = 0; i < idGroup.size(); i++) {
                for (int j = i + 1; j < idGroup.size(); j++) {
                    ComponentScope a = idGroup.get(i), b = idGroup.get(j);
                    if (isDuplicated(a, b)) {
                        // TODO: Check whether the components is combinational
                        var iterator = b.getOutputs().iterator();
                        for (var rep : a.getOutputs()) {
                            set.union(iterator.next(), rep);
                        }
                        toRemove.add(b);
                    }
                }
            }
        }
        scope.getChildren().removeAll(toRemove);
    }

    List<ComponentScope> collectDuplicatedTo(ComponentScope scope) {
        List<ComponentScope> toRemove = new ArrayList<>();
        for (var child : scope.getChildren()) {
            var component = (ComponentScope) child;
            if (component.isBuiltin() && BuiltinComponent.of(component.getIdentifier()) == TO) {
                var input = component.getInputs().getFirst();
                var output = component.getOutputs().getFirst();
                if (input.equals(output)) {
                    toRemove.add(component);
                }
            }
        }
        return toRemove;
    }

    boolean isDuplicated(ComponentScope a, ComponentScope b) {
        if (a.getIdentifier().equals(b.getIdentifier())) {
            if (hasSameAttribute(a, b)) {
                if (a.getInputs().equals(b.getInputs())) {
                    var aoIter = a.getOutputs().iterator();
                    var boIter = b.getOutputs().iterator();
                    while (aoIter.hasNext() && boIter.hasNext()) {
                        if (!aoIter.next().getType().equals(boIter.next().getType())) {
                            return false;
                        }
                    }
                    return !(aoIter.hasNext() || boIter.hasNext());
                }
            }
        }
        return false;
    }

    boolean hasSameAttribute(ComponentScope a, ComponentScope b) {
        if (a.hasAttributes()) {
            return a.getAttributes().equals(b.getAttributes());
        } else {
            return !b.hasAttributes();
        }
    }

    public boolean needRemove(Entity entity) {
        if (entity instanceof Variable variable) {
            if (set.contains(variable)) {
                return set.find(variable) != variable;
            }
        }
        return false;
    }

    public Variable repOf(Variable variable) {
        if (set.contains(variable)) {
            return set.find(variable);
        }
        return variable;
    }
}
