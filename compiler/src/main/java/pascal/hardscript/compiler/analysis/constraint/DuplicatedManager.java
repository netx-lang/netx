package pascal.hardscript.compiler.analysis.constraint;

import pascal.hardscript.compiler.model.Entity;
import pascal.hardscript.compiler.model.instance.Instance;
import pascal.hardscript.compiler.model.scope.ComponentScope;
import pascal.hardscript.util.collection.UnionFind;
import pascal.hardscript.util.lang.BuiltinComponent;
import pascal.hardscript.util.lang.Identifier;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static pascal.hardscript.util.lang.BuiltinComponent.NO_DUMP;

public class DuplicatedManager {
    private final UnionFind<Instance> set = new UnionFind<>();

    public void collectDuplicatedComponents(ComponentScope scope) {
        Map<Identifier, List<ComponentScope>> idGroups = scope.getChildren().stream()
                .collect(Collectors.groupingBy(ComponentScope::getIdentifier));

        List<ComponentScope> toRemove = new LinkedList<>();
        for (var entry : idGroups.entrySet()) {
            var identifier = entry.getKey();
            if (BuiltinComponent.isBuiltin(identifier)
                    && NO_DUMP.contains(BuiltinComponent.of(identifier.name()))) {
                continue;
            }

            var idGroup = entry.getValue();
            for (int i = 0; i < idGroup.size(); i++) {
                for (int j = i + 1; j < idGroup.size(); j++) {
                    ComponentScope a = idGroup.get(i), b = idGroup.get(j);
                    if (isDuplicated(a, b)) {
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

    boolean isDuplicated(ComponentScope a, ComponentScope b) {
        if (a.getIdentifier().equals(b.getIdentifier())) {
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
        return false;
    }

    public boolean needRemove(Entity entity) {
        if (entity instanceof Instance instance) {
            if (set.contains(instance)) {
                return set.find(instance) != instance;
            }
        }
        return false;
    }

    public Instance repOf(Instance instance) {
        if (set.contains(instance)) {
            return set.find(instance);
        }
        return instance;
    }
}
