package pascal.hardscript.compiler.analysis;

import pascal.hardscript.compiler.model.Entity;
import pascal.hardscript.compiler.model.instance.Instance;
import pascal.hardscript.compiler.model.scope.ComponentScope;
import pascal.hardscript.compiler.model.scope.DesignScope;
import pascal.hardscript.compiler.model.scope.Scope;
import pascal.hardscript.util.collection.UnionFind;
import pascal.hardscript.util.lang.BuiltinComponent;
import pascal.hardscript.util.lang.Identifier;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static pascal.hardscript.util.lang.BuiltinComponent.NO_DUMP;

public class DuplicatedElimination extends ModelAnalysis {
    private final UnionFind<Instance> duplicatedRep = new UnionFind<>();

    public DuplicatedElimination(DesignScope design) {
        super(design);
    }

    @Override
    public void analyze() {
        design.forEachScope(this::inline);
        design.forEachScope(this::collectDuplicatedComponents);
        design.forEachScope(this::replaceDuplicatedInstances);
    }

    private void inline(Scope scope) {
        List<Scope> inlinedChildren = new LinkedList<>();
        var binding = scope.getBinding();
        var aliases = scope.getAliases();

        for (var child : scope.getChildren()) {
            inline(child);
            if (child instanceof ComponentScope) {
                inlinedChildren.add(child);
            } else {
                binding.add(child.getBinding());
                aliases.addAll(child.getAliases());
                inlinedChildren.addAll(child.getChildren());
                for (var grandChild : child.getChildren()) {
                    grandChild.setParent(scope);
                }
            }
        }
        scope.setChildren(inlinedChildren);
    }

    private void collectDuplicatedComponents(Scope scope) {
        Map<Identifier, List<ComponentScope>> idGroups = scope.getChildren().stream()
                .map(ComponentScope.class::cast)
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
                ComponentScope a = idGroup.get(i);
                for (int j = i + 1; j < idGroup.size(); j++) {
                    ComponentScope b = idGroup.get(j);
                    var iterator = b.getOutputs().iterator();
                    if (isDuplicated(a, b)) {
                        toRemove.add(b);
                        for (var rep : a.getOutputs()) {
                            var dup = iterator.next();
                            if (dup != rep) {
                                duplicatedRep.union(dup, rep);
                            }
                        }
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

    private void replaceDuplicatedInstances(Scope scope) {
        scope.getEntities().removeIf(this::needRemove);
        if (scope instanceof ComponentScope componentScope) {
            componentScope.getInputs().replaceAll(this::repOf);
            componentScope.getOutputs().replaceAll(this::repOf);
        }
    }

    private boolean needRemove(Entity entity) {
        if (entity instanceof Instance instance) {
            if (duplicatedRep.contains(instance)) {
                return duplicatedRep.find(instance) != instance;
            }
        }
        return false;
    }

    private Instance repOf(Instance instance) {
        if (duplicatedRep.contains(instance)) {
            return duplicatedRep.find(instance);
        }
        return instance;
    }

}
