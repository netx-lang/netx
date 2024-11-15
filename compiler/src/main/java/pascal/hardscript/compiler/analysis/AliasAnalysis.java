package pascal.hardscript.compiler.analysis;

import pascal.hardscript.compiler.model.Entity;
import pascal.hardscript.compiler.model.instance.Data;
import pascal.hardscript.compiler.model.instance.DataAccess;
import pascal.hardscript.compiler.model.instance.Instance;
import pascal.hardscript.compiler.model.instance.Value;
import pascal.hardscript.compiler.model.scope.ComponentScope;
import pascal.hardscript.compiler.model.scope.DesignScope;
import pascal.hardscript.compiler.model.scope.Scope;
import pascal.hardscript.util.collection.UnionFind;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Predicate;

import static pascal.hardscript.compiler.Configuration.FULLY_EXPAND;

public final class AliasAnalysis extends ModelAnalysis {
    private final AliasStructure global = new AliasStructure();

    public AliasAnalysis(DesignScope design) {
        super(design);
    }

    @Override
    public void analyze() {
        if (FULLY_EXPAND) {
            globalAnalyze();
        } else {
            scopeWiseAnalyze();
        }
    }

    private void globalAnalyze() {
        design.forEachScope(scope -> global.addAlias(scope.getAliases()));
        global.resolveRepresentatives();
        design.forEachScope(scope -> {
            scope.getEntities().removeIf(Predicate.not(global::needDump));
            if (scope instanceof ComponentScope componentScope) {
                componentScope.getInputs().replaceAll(global::repOf);
                componentScope.getOutputs().replaceAll(global::repOf);
            }
        });
    }

    private void scopeWiseAnalyze() {
        design.forEachScope(scope -> {
            if (isUserDefinedComponent(scope)) {
                new ComponentScopeWiseAnalysis(scope).analyze();
            }
        });
        global.resolveRepresentatives();
        design.forEachScope(scope -> {
            if (isUserDefinedComponent(scope)) {
                var component = (ComponentScope) scope;
                component.getInputs().replaceAll(global::repOf);
                component.getOutputs().replaceAll(global::repOf);
            }
        });
    }

    private static boolean isUserDefinedComponent(Scope scope) {
        return scope instanceof ComponentScope component && !component.isBuiltin();
    }

    private class ComponentScopeWiseAnalysis {
        private final Scope scope;
        private final AliasStructure local = new AliasStructure();
        private final List<Scope> analysisScopes = new LinkedList<>();

        private ComponentScopeWiseAnalysis(Scope scope) {
            this.scope = scope;
        }

        public void analyze() {
            exploreAnalysisScopes(scope);
            local.resolveRepresentatives();
            eliminateDuplicatedData();
        }

        private void exploreAnalysisScopes(Scope scope) {
            analysisScopes.add(scope);
            local.addAlias(scope.getAliases());
            global.addAlias(scope.getAliases());
            for (var child : scope.getChildren()) {
                if (!isUserDefinedComponent(child)) {
                    exploreAnalysisScopes(child);
                }
            }
        }

        public void eliminateDuplicatedData() {
            analysisScopes.forEach(scope -> {
                scope.getEntities().removeIf(Predicate.not(local::needDump));
                if (scope instanceof ComponentScope componentScope) {
                    componentScope.getInputs().replaceAll(local::repOf);
                    componentScope.getOutputs().replaceAll(local::repOf);
                }
            });
        }
    }

    public static class AliasStructure {
        private final UnionFind<Instance> set = new UnionFind<>();
        private final Map<Instance, Instance> sourceRep = new HashMap<>();

        void addAlias(Collection<Scope.Alias> aliases) {
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

        boolean needUpdate(Instance now, Instance old) {
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
        Instance getNextRep(Instance inst) {
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
}
