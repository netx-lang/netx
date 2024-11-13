package pascal.hardscript.compiler.analysis;

import pascal.hardscript.compiler.model.Entity;
import pascal.hardscript.compiler.model.instance.Data;
import pascal.hardscript.compiler.model.instance.DataAccess;
import pascal.hardscript.compiler.model.instance.Instance;
import pascal.hardscript.compiler.model.instance.Value;
import pascal.hardscript.compiler.model.scope.Scope;
import pascal.hardscript.util.collection.UnionFind;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AliasStructure {
    private final UnionFind<Instance> set = new UnionFind<>();
    private final Map<Instance, Instance> sourceRep = new HashMap<>();

    public void addAlias(Collection<Scope.Alias> aliases) {
        for (var alias : aliases) {
            set.union(alias.lhs(), alias.rhs());
        }
    }

    public void resolveRepresentatives() {
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
            var nowData = now instanceof DataAccess access ? access.base() : (Data) now;
            var oldData = old instanceof DataAccess access ? access.base() : (Data) old;
            return nowData.getIndex() < oldData.getIndex();
        }
    }

    public boolean needDump(Entity entity) {
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
            var base = access.base();
            if (hasSourceAlias(base) && repOf(base) != base) {
                return Instance.accessOf(repOf(base), access.selector());
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
