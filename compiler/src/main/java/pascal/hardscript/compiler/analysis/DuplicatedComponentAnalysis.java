package pascal.hardscript.compiler.analysis;

import pascal.hardscript.compiler.ast.Identifier;
import pascal.hardscript.compiler.model.Entity;
import pascal.hardscript.compiler.model.instance.Instance;
import pascal.hardscript.compiler.model.scope.ComponentInfo;
import pascal.hardscript.compiler.model.scope.ComponentScope;
import pascal.hardscript.compiler.model.scope.DesignScope;
import pascal.hardscript.compiler.model.scope.Scope;
import pascal.hardscript.util.lang.BuiltinComponent;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static pascal.hardscript.util.lang.BuiltinComponent.NO_DUMP;

public class DuplicatedComponentAnalysis extends ModelAnalysis {
    private final Map<Entity, Instance> duplicatedRep = new HashMap<>();
    private final ComponentInfo info;

    public DuplicatedComponentAnalysis(DesignScope design) {
        super(design);
        info = design.getComponentInfo();
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
            if (info.containsBuiltin(identifier)
                    && NO_DUMP.contains(BuiltinComponent.of(identifier.string()))) {
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
                                duplicatedRep.put(dup, rep);
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
        scope.getEntities().removeIf(duplicatedRep::containsKey);
        if (scope instanceof ComponentScope componentScope) {
            componentScope.getInputs().replaceAll(this::repOf);
            componentScope.getOutputs().replaceAll(this::repOf);
        }
    }

    private Instance repOf(Instance instance) {
        if (duplicatedRep.containsKey(instance)) {
            return duplicatedRep.get(instance);
        }
        return instance;
    }

}
