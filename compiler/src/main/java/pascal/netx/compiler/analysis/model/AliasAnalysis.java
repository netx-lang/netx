package pascal.netx.compiler.analysis.model;

import pascal.netx.compiler.model.Entity;
import pascal.netx.compiler.model.scope.ComponentScope;
import pascal.netx.compiler.model.scope.PackageScope;
import pascal.netx.compiler.model.variable.Access;
import pascal.netx.compiler.model.variable.Mutable;
import pascal.netx.compiler.model.variable.Value;
import pascal.netx.compiler.model.variable.Variable;
import pascal.netx.compiler.model.variable.type.BitType;
import pascal.netx.util.collection.UnionFind;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Analyzes alias relations in a package, propagating the intra-component alias relations to other components.
 * This is used for expanded dump of the design, where component instantiations are expanded.
 */
public final class AliasAnalysis extends ModelAnalysis {
    private final AliasManager manager = new AliasManager();

    public AliasAnalysis(PackageScope design) {
        super(design);
    }

    @Override
    public void analyze() {
        pack.forEachSubComponent(manager::addAliasForScope);
        manager.resolveRepresentatives();
        pack.forEachSubComponent(scope -> {
            scope.getBinding().releaseIf(Predicate.not(manager::needDump));
            scope.getInputs().replaceAll(manager::repOf);
            scope.getOutputs().replaceAll(manager::repOf);
            if (scope.hasAttributes()) {
                scope.getAttributes().replaceAllVariable(manager::repOf);
            }
            manager.clearIn(scope);
        });
    }

    /**
     * Manage alias relations introduced by either named or anonymous wires.
     * This class maintains equivalence classes of these relations,
     * and heuristically resolves the representatives of each class (by{@link #needUpdate})
     * for better readability of the dumped design.
     * <p>
     * After replacing the variables with their representatives,
     * this class eliminates unnecessary alias relations in {@link #clearIn}.
     */
    public static final class AliasManager {
        private final UnionFind<Variable> set = new UnionFind<>();
        private final Map<Variable, Variable> sourceRep = new HashMap<>();

        static boolean isSelfAlias(Variable variable) {
            if (variable instanceof Access(var base, var selector)) {
                if (base.getType() instanceof BitType(int width)) {
                    return width == selector.getWidth();
                }
            }
            return false;
        }

        void addAliasForScope(ComponentScope scope) {
            List<Access> selfAlias = new ArrayList<>();
            for (var alias : scope.getAliases()) {
                if (isSelfAlias(alias.lhs())) {
                    selfAlias.add((Access) alias.lhs());
                }
                if (isSelfAlias(alias.rhs())) {
                    selfAlias.add((Access) alias.rhs());
                }
                set.union(alias.lhs(), alias.rhs());
            }
            for (var access : selfAlias) {
                set.union(access, access.base());
            }
        }

        /**
         * Clear the alias relations in the given scope, only keep the necessary ones,
         * i.e., multiple variables in the equivalence class of both sides need to be dumped.
         * <p>
         * This happens only when selections are involved in the alias relations,
         * e.g., there is a relation {@code Alias(a[3], b)}
         * with both {@code a} of bit(4) and {@code b} of bit(1) need to be dumped.
         */
        void clearIn(ComponentScope scope) {
            Set<ComponentScope.Alias> newAlias = new LinkedHashSet<>();
            Consumer<ComponentScope.Alias> addNew = (alias) -> {
                if (!newAlias.contains(alias) && !newAlias.contains(alias.flip())) {
                    newAlias.add(alias);
                }
            };
            for (var alias : scope.getAliases()) {
                if (alias.lhs() instanceof Access access && needDump(access.base())) {
                    if (isValidDump(alias.rhs())) {
                        addNew.accept(new ComponentScope.Alias(access, alias.rhs()));
                    } else if (isNotIdentity(repOf(alias.rhs()), access)) {
                        addNew.accept(new ComponentScope.Alias(access, repOf(alias.rhs())));
                    }
                } else if (alias.rhs() instanceof Access access){
                    var lhs = repOf(alias.lhs());
                    if (isValidDump(alias.rhs()) && isNotIdentity(lhs, access)) {
                        addNew.accept(new ComponentScope.Alias(lhs, alias.rhs()));
                    } else {
                        var rhs = repOf(alias.rhs());
                        if (isValidDump(rhs) && isNotIdentity(lhs, rhs)) {
                            addNew.accept(new ComponentScope.Alias(lhs, rhs));
                        }
                    }
                }
            }
            scope.getAliases().clear();
            scope.getAliases().addAll(newAlias);
        }

        boolean isValidDump(Variable variable) {
            return needDump(variable) || (variable instanceof Access access && needDump(access.base()));
        }

        private boolean isNotIdentity(Variable lhs, Variable rhs) {
            return !lhs.equals(rhs)
                    && (!(rhs instanceof Access access)
                            || (!isSelfAlias(access) || !access.base().equals(lhs)));
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
                var baseRep = repOf(base);
                if (hasSourceAlias(base) && baseRep != base) {
                    if (baseRep.getType() instanceof BitType(int width)) {
                        if (selector.getWidth() == width) {
                            return baseRep;
                        }
                    }
                    return Variable.accessOf(baseRep, selector);
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
}
