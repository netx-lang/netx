package pascal.netx.compiler.analysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pascal.netx.compiler.model.Entity;
import pascal.netx.compiler.model.Signature;
import pascal.netx.compiler.model.scope.ComponentScope;
import pascal.netx.compiler.model.variable.Access;
import pascal.netx.compiler.model.variable.Mutable;
import pascal.netx.compiler.model.variable.Value;
import pascal.netx.compiler.model.variable.Variable;
import pascal.netx.util.collection.UnionFind;
import pascal.netx.util.exception.ShallNotReachHereError;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AliasManager {
    private static final Logger log = LoggerFactory.getLogger(AliasManager.class);
    private final UnionFind<Variable> set = new UnionFind<>();
    private final Map<Variable, Variable> sourceRep = new HashMap<>();

    void addAlias(Collection<ComponentScope.Alias> aliases) {
        for (var alias : aliases) {
            set.union(alias.lhs(), alias.rhs());
        }
    }

    void resolveRepresentatives() {
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

    void setAsSourceRep(Signature sign, Variable inst) {
        if (!inst.isUserDefined()) {
            throw new ShallNotReachHereError("Cannot set a non-user-defined instance as a source representative");
        }
        if (set.contains(inst)) {
            var root = set.find(inst);
            if (sourceRep.get(root) instanceof Value) {
                log.warn("Component {} has constant IO {}",
                        sign.caller().getIdentifier().toStringWithLoc(), inst);
            } else {
                sourceRep.put(root, inst);
            }
        }
    }

    boolean needDump(Entity entity) {
        if (entity instanceof Variable inst) {
            if (inst.isUserDefined()) {
                return repOf(inst) == inst;
            } else {
                return !hasSourceAlias(inst) && set.find(inst) == inst;
            }
        }
        return false;
    }

    boolean needUpdate(Variable now, Variable old) {
        // Prefer Value to Data
        if (old instanceof Value) {
            return false;
        } else if (now instanceof Value) {
            return true;
        } else {
            return ((Mutable) now).getIndex() < ((Mutable) old).getIndex();
        }
    }

    boolean hasSourceAlias(Variable inst) {
        if (set.contains(inst)) {
            return sourceRep.containsKey(set.find(inst));
        }
        return false;
    }

    @Nonnull
    Variable repOf(Variable inst) {
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
    Variable getNextRep(Variable inst) {
        // If the inst is a DataAccess, find the alias of its base
        if (inst instanceof Access(var base, var selector)) {
            if (hasSourceAlias(base) && repOf(base) != base) {
                return Variable.accessOf(repOf(base), selector);
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
