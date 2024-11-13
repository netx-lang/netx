package pascal.hardscript.compiler.analysis;

import pascal.hardscript.compiler.model.Entity;
import pascal.hardscript.compiler.model.instance.Data;
import pascal.hardscript.compiler.model.instance.DataAccess;
import pascal.hardscript.compiler.model.instance.Instance;
import pascal.hardscript.compiler.model.instance.Value;
import pascal.hardscript.compiler.model.scope.ComponentScope;
import pascal.hardscript.compiler.model.scope.DesignScope;
import pascal.hardscript.util.collection.UnionFind;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

public final class DuplicatedDataElimination extends ModelAnalysis {
    private final UnionFind<Instance> set = new UnionFind<>();
    private final Map<Instance, Instance> sourceRep = new HashMap<>();

    public DuplicatedDataElimination(DesignScope design) {
        super(design);
    }

    @Override
    public void analyze() {
        collectAlias();
        eliminateDuplicatedData();
        propagateAliasType();
        clearAlias();
    }

    private void collectAlias() {
        design.forEachScope(scope -> {
            for (var alias : scope.getAliases()) {
                set.union(alias.from(), alias.to());
            }
        });
        for (var element : set.elements()) {
            // Set user-defined instances as the representatives for the generated instances
            if (element.isUserDefined()) {
                var root = set.find(element);
                // If there are multiple user-defined instances, decide which to use
                if (!sourceRep.containsKey(root) || needUpdate(element, sourceRep.get(root))) {
                    sourceRep.put(root, element);
                }
            }
        }
    }

    private boolean needUpdate(Instance now, Instance old) {
        // Prefer Value over Data
        if (old instanceof Value) {
            return false;
        } else if (now instanceof Value) {
            return true;
        } else {
            return ((Data) now).getIndex() < ((Data) old).getIndex();
        }
    }

    public void eliminateDuplicatedData() {
        design.forEachScope(scope -> {
            scope.getEntities().removeIf(Predicate.not(this::needDump));
            if (scope instanceof ComponentScope componentScope) {
                componentScope.getInputs().replaceAll(this::repOf);
                componentScope.getOutputs().replaceAll(this::repOf);
            }
        });
    }

    public void propagateAliasType() {
        // First pass: propagate type from alias to root
        for (var element : set.elements()) {
            var root = set.find(element);
            if (!element.hasImplicitType() && root.hasImplicitType()) {
                root.setType(element.getType());
            }
        }
        // Second pass: propagate type from root to alias
        for (var element : set.elements()) {
            var root = set.find(element);
            if (element.hasImplicitType() && !root.hasImplicitType()) {
                element.setType(root.getType());
            }
        }
    }

    private void clearAlias() {
        design.forEachScope(scope -> scope.getAliases().clear());
    }

    private boolean needDump(Entity entity) {
        if (entity instanceof Instance inst) {
            if (inst.isUserDefined()) {
                return repOf(inst) == inst;
            } else {
                return !hasSourceAlias(inst) && set.find(inst) == inst;
            }
        }
        return false;
    }


    boolean hasSourceAlias(Instance inst) {
        if (set.contains(inst)) {
            return sourceRep.containsKey(set.find(inst));
        }
        return false;
    }

    @Nonnull
    Instance repOf(Instance inst) {
        // We need to keep trying to get the representative until reach a fixed point,
        // since there may have a representation chain such as x -> y[2:0] -> z[2:0]
        while (true) {
            var next = getNextRep(inst);
            if (next == inst) {
                return inst;
            }
            inst = next;
        }
    }

    @Nonnull
    private Instance getNextRep(Instance inst) {
        // If the inst is a DataAccess, find the alias of its base
        if (inst instanceof DataAccess access) {
            var base = access.getBase();
            if (hasSourceAlias(base) && repOf(base) != base) {
                return DataAccess.of(repOf(base), access.getSelector());
            }
        }
        // Otherwise find the alias of the inst itself
        if (set.contains(inst)) {
            var result = set.find(inst);
            return Objects.requireNonNullElse(sourceRep.get(result), result);
        }
        return inst;
    }
}
