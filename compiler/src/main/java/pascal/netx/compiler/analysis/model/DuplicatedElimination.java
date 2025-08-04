package pascal.netx.compiler.analysis.model;

import pascal.netx.compiler.model.Entity;
import pascal.netx.compiler.model.scope.ComponentScope;
import pascal.netx.compiler.model.scope.PackageScope;
import pascal.netx.compiler.model.variable.Mutable;
import pascal.netx.compiler.model.variable.Value;
import pascal.netx.compiler.model.variable.Variable;
import pascal.netx.util.collection.UnionFind;
import pascal.netx.util.lang.BuiltinComponent;
import pascal.netx.util.lang.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static pascal.netx.util.lang.BuiltinComponent.TO;

/**
 * This analysis eliminates duplicated components within a package.
 * It identifies components that are structurally identical and replaces
 * them with a single representative instance, reducing redundancy in the model.
 * <p>
 * Used for expanded dump of the design, where component instantiations are not expanded
 * and duplication introduced by them is considered.
 */
public final class DuplicatedElimination extends ModelAnalysis {
    private final DuplicatedManager manager = new DuplicatedManager();

    public DuplicatedElimination(PackageScope design) {
        super(design);
    }

    @Override
    public void analyze() {
        pack.forEachSubComponent(manager::collectDuplicatedComponents);
        manager.resolveRepresentatives();
        pack.forEachSubComponent(this::replaceDuplicatedInstances);
    }

    private void replaceDuplicatedInstances(ComponentScope scope) {
        scope.getBinding().releaseIf(manager::needRemove);
        scope.getInputs().replaceAll(manager::repOf);
        scope.getOutputs().replaceAll(manager::repOf);
        if (scope.hasAttributes()) {
            scope.getAttributes().replaceAllVariable(manager::repOf);
        }
    }

    /**
     * Manages the detection and resolution of duplicated components in a design.
     * Duplicated components are those have the same identifier, inputs, and attributes,
     * by which we can infer their outputs are actually alias.
     */
    public static class DuplicatedManager {
        private final UnionFind<Variable> set = new UnionFind<>();
        private final Map<Variable, Variable> rep = new HashMap<>();

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
            for (var child : toRemove) {
                scope.getBinding().bind(child.getBinding());
                scope.getChildren().remove(child);
            }
        }

        public void resolveRepresentatives() {
            for (var element : set.elements()) {
                var root = set.find(element);
                if (!rep.containsKey(root) || needUpdate(element, rep.get(root))) {
                    rep.put(root, element);
                }
            }
        }

        // FIXME: duplicated to AliasManager::needUpdate
        boolean needUpdate(Variable now, Variable old) {
            if (old instanceof Value) {
                return false;
            } else if (now instanceof Value) {
                return true;
            } else {
                return ((Mutable) now).getIndex() < ((Mutable) old).getIndex();
            }
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
                    return rep.get(set.find(variable)) != variable;
                }
            }
            return false;
        }

        public Variable repOf(Variable variable) {
            if (set.contains(variable)) {
                return rep.get(set.find(variable));
            }
            return variable;
        }
    }
}
