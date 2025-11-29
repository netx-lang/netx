package pascal.netx.compiler.backend.analysis.model;

import pascal.netx.compiler.circuit.Entity;
import pascal.netx.compiler.circuit.scope.ComponentScope;
import pascal.netx.compiler.circuit.scope.PackageScope;
import pascal.netx.compiler.circuit.signal.*;
import pascal.netx.compiler.circuit.signal.selector.Selector;
import pascal.netx.compiler.circuit.signal.type.BitType;
import pascal.netx.compiler.io.Logger;
import pascal.netx.compiler.utils.CompileCancellationException;
import pascal.netx.util.collection.UnionFind;
import pascal.netx.util.lang.BuiltinComponent;

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
        new BuiltinAnalysis().analyze();
        manager.sourceRep.clear();
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
        private final UnionFind<Signal> set = new UnionFind<>();
        private final Map<Signal, Signal> sourceRep = new HashMap<>();

        static boolean isSelfAlias(Signal signal) {
            if (signal instanceof Access(var base, var selector)) {
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

        boolean isValidDump(Signal signal) {
            return needDump(signal) || (signal instanceof Access access && needDump(access.base()));
        }

        private boolean isNotIdentity(Signal lhs, Signal rhs) {
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
            if (entity instanceof Signal inst) {
                if (inst.isUserDefined()) {
                    return repOf(inst) == inst;
                } else {
                    return !hasSourceAlias(inst) && set.find(inst) == inst;
                }
            }
            return false;
        }

        boolean needUpdate(Signal now, Signal old) {
            // Prefer Value to Data
            if (old instanceof Value) {
                return false;
            } else if (now instanceof Value) {
                return true;
            } else {
                return ((Data) now).getIndex() < ((Data) old).getIndex();
            }
        }

        boolean hasSourceAlias(Signal inst) {
            if (set.contains(inst)) {
                return sourceRep.containsKey(set.find(inst));
            }
            return false;
        }

        @Nonnull
        Signal repOf(Signal inst) {
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
        Signal getNextRep(Signal inst) {
            // If the inst is a DataAccess, find the alias of its base
            if (inst instanceof Access(var base, var selector)) {
                var baseRep = repOf(base);
                if (hasSourceAlias(base) && baseRep != base) {
                    if (baseRep.getType() instanceof BitType(int width)) {
                        if (selector.getWidth() == width) {
                            return baseRep;
                        }
                    }
                    return Signal.accessOf(baseRep, selector);
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

    /**
     * This analysis extracts alias relations from built-in components.
     * There are three cases:
     * <li> In {@code CAST} components, the input is truncated, extended, or directly passed to the output;
     * we explicitly add the alias relation between the output and the input (or a truncated input).
     * <li> In {@code CONCAT} components, if there is only one input,
     * we add an alias relation between the output and the input since there is no concatenation actually happening.
     * <li> In {@code AND}, {@code NAND}, {@code OR}, {@code NOR}, and {@code XOR} components,
     * we analyze the inputs and outputs to determine if they can be simplified to a direct alias
     * (e.g., if one input is a constant high, the other input can be directly passed to the output for an AND gate).
     * <li> In {@code TO} components, the elaborator introduces relations
     * like {@code i <> $TO_DUMMY}. and {@code $TO_DUMMY <> o};
     * we inline these relations to make them direct aliases {@code i <> o}.
     */
    public final class BuiltinAnalysis {
        private void addBuiltinAlias(ComponentScope scope, Signal lhs, Signal rhs) {
            manager.set.union(lhs, rhs);
            scope.getAliases().add(new ComponentScope.Alias(lhs, rhs));
        }

        public void analyze() {
            pack.forEachSubComponent(scope -> {
                if (scope.isBuiltin()) {
                    var builtinRef = BuiltinComponent.of(scope.getIdentifier());
                    switch (builtinRef) {
                        case CAST, UCAST -> extractFromCast(scope);
                        case CONCAT -> extractFromConcat(scope);
                        case OR -> extractFromOr(scope);
                        case NOR -> extractFromNor(scope);
                        case XOR -> extractFromXor(scope);
                        case AND -> extractFromAnd(scope);
                        case NAND -> extractFromNand(scope);
                    }
                }
            });
            pack.forEachSubComponent(this::inlineIndirectToAlias);
        }

        private void extractFromCast(ComponentScope scope) {
            var lhs = scope.getOutputs().getFirst();
            var rhs = scope.getInputs().getFirst();
            if (!(lhs.getType() instanceof BitType(int lhsW)) || !(rhs.getType() instanceof BitType(int rhsW))) {
                Logger.error("Unexpected type for CAST component: {} -> {}", lhs.getType(), rhs.getType());
                throw new CompileCancellationException();
            }
            if (lhsW == rhsW) {
                addBuiltinAlias(scope, lhs, rhs);
            } else if (lhsW < rhsW) {
                var truncated = Signal.accessOf(rhs, Selector.ofWidth(lhsW));
                addBuiltinAlias(scope, lhs, truncated);
            }
        }

        private void extractFromConcat(ComponentScope scope) {
            if (scope.getInputs().size() == 1) {
                var lhs = scope.getOutputs().getFirst();
                var rhs = scope.getInputs().getFirst();
                addBuiltinAlias(scope, lhs, rhs);
            }
        }

        private void extractFromAnd(ComponentScope scope) {
            if (scope.getInputs().size() == 2) {
                var inputA = scope.getInputs().get(0);
                var inputB = scope.getInputs().get(1);

                if (allHigh(inputA)) {
                    setAsTo(scope, inputB);
                } else if (allHigh(inputB)) {
                    setAsTo(scope, inputA);
                }
            }
        }

        private void extractFromOr(ComponentScope scope) {
            if (scope.getInputs().size() == 2) {
                var inputA = scope.getInputs().get(0);
                var inputB = scope.getInputs().get(1);

                if (allLow(inputA)) {
                    setAsTo(scope, inputB);
                } else if (allLow(inputB)) {
                    setAsTo(scope, inputA);
                }
            }
        }

        private void extractFromNand(ComponentScope scope) {
            if (scope.getInputs().size() == 2) {
                var inputA = scope.getInputs().get(0);
                var inputB = scope.getInputs().get(1);

                if (allHigh(inputA)) {
                    setAsNot(scope, inputB);
                } else if (allHigh(inputB)) {
                    setAsNot(scope, inputA);
                }
            }
        }

        private void extractFromNor(ComponentScope scope) {
            if (scope.getInputs().size() == 2) {
                var inputA = scope.getInputs().get(0);
                var inputB = scope.getInputs().get(1);

                if (allLow(inputA)) {
                    setAsNot(scope, inputB);
                } else if (allLow(inputB)) {
                    setAsNot(scope, inputA);
                }
            }
        }

        private void extractFromXor(ComponentScope scope) {
            if (scope.getInputs().size() == 2) {
                var inputA = scope.getInputs().get(0);
                var inputB = scope.getInputs().get(1);

                if (allLow(inputA)) {
                    setAsTo(scope, inputB);
                } else if (allHigh(inputA)) {
                    setAsNot(scope, inputB);
                } else if (allLow(inputB)) {
                    setAsTo(scope, inputA);
                } else if (allHigh(inputB)) {
                    setAsNot(scope, inputA);
                }
            }
        }

        private void setAsTo(ComponentScope scope, Signal input) {
            transformComponent(scope, BuiltinComponent.TO, input);
            addBuiltinAlias(scope, input, scope.getOutputs().getFirst());
        }

        private void setAsNot(ComponentScope scope, Signal input) {
            transformComponent(scope, BuiltinComponent.NOT, input);
        }

        private void transformComponent(ComponentScope scope, BuiltinComponent builtin, Signal input) {
            scope.setInputs(new ArrayList<>(List.of(input)));
            scope.setIdentifier(builtin.asID());
        }

        private boolean allLow(Signal signal) {
            signal = manager.repOf(signal);
            if (signal instanceof Value value) {
                return switch (value) {
                    case BooleanValue _ -> false;
                    case VectorValue v -> v.isZero();
                };
            }
            return false;
        }

        private boolean allHigh(Signal signal) {
            signal = manager.repOf(signal);
            if (signal instanceof Value value) {
                return switch (value) {
                    case BooleanValue _ -> false;
                    case VectorValue v -> v.value().not().isZero();
                };
            }
            return false;
        }

        /**
         * In TO components, there are alias relations i -> dummy and dummy -> o.
         * This indirect alias may cause further problems, so we inline them here.
         */
        private void inlineIndirectToAlias(ComponentScope scope) {
            var dummies = collectDummies(scope);

            // Collect non-dummy <-> dummy relations
            var dummyToRhs = collectNonDummies(scope, dummies, true);
            var dummyToLhs = collectNonDummies(scope, dummies, false);

            // Build direct alias for non-dummy <-> non-dummy relations
            for (var dummy : dummies.representatives()) {
                var lhsList = dummyToLhs.get(dummy);
                var rhsList = dummyToRhs.get(dummy);
                if (lhsList == null || rhsList == null) continue;
                for (var lhs : lhsList) {
                    for (var rhs : rhsList) {
                        addBuiltinAlias(scope, lhs, rhs);
                    }
                }
            }
        }

        // Find all dummies and group them using UnionFind
        private UnionFind<Signal> collectDummies(ComponentScope scope) {
            var dummies = new UnionFind<Signal>();
            for (var alias : scope.getAliases()) {
                boolean dummyLhs = isToDummy(alias.lhs());
                boolean dummyRhs = isToDummy(alias.rhs());
                if (dummyLhs) dummies.add(alias.lhs());
                if (dummyRhs) dummies.add(alias.rhs());
                if (dummyLhs && dummyRhs) dummies.union(alias.lhs(), alias.rhs());
            }
            return dummies;
        }

        private HashMap<Signal, List<Signal>> collectNonDummies(
                ComponentScope scope, UnionFind<Signal> dummies, boolean lhsIsDummy) {
            var dummyToOther = new HashMap<Signal, List<Signal>>();
            for (var alias : scope.getAliases()) {
                Signal dummy = lhsIsDummy ? alias.lhs() : alias.rhs();
                Signal other = lhsIsDummy ? alias.rhs() : alias.lhs();
                if (isToDummy(dummy) && !isToDummy(other)) {
                    dummyToOther.computeIfAbsent(dummies.find(dummy), _ -> new ArrayList<>()).add(other);
                }
            }
            return dummyToOther;
        }

        private boolean isToDummy(Signal var) {
            return var instanceof Wire wire && wire.getIdentifier().name().equals("$TO_DUMMY");
        }
    }
}
