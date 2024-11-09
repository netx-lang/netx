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

public final class AliasAnalysis extends ModelAnalysis {
    private final UnionFind<Instance> aliases = new UnionFind<>();
    private final Map<Instance, Instance> sourceRep = new HashMap<>();

    public AliasAnalysis(DesignScope design) {
        super(design);
    }

    @Override
    public void analyze() {
        collectAlias();
        eliminateDuplicate();
        inferAliasType();
        design.forEachScope(scope -> scope.getAliases().clear());
    }

    private void collectAlias() {
        design.forEachScope(scope -> scope.getAliases()
                .forEach(alias -> aliases.union(alias.from(), alias.to())));
        for (var element : aliases.elements()) {
            if (element.isUserDefined()) {
                var root = aliases.find(element);
                if (needUpdate(element, root)) {
                    sourceRep.put(root, element);
                }
            }
        }
    }

    private boolean needUpdate(Instance element, Instance root) {
        // FIXME: Better ordering
        if (sourceRep.containsKey(root)) {
            var top = sourceRep.get(root);
            return switch (top) {
                case Value ignored -> false;
                case Data ignored -> {
                    if (element instanceof Value) yield true;
                    if (element.getDepth() == top.getDepth()) {
                        boolean eAccFlag = element instanceof DataAccess;
                        boolean tAccFlag = top instanceof DataAccess;
                        if (eAccFlag == tAccFlag) {
                            yield element.getSourceLoc().compareTo(top.getSourceLoc()) > 0;
                        }
                        yield eAccFlag;
                    }
                    yield element.getDepth() < top.getDepth();
                }
            };
        }
        return true;
    }

    public void eliminateDuplicate() {
        design.forEachScope(scope -> {
            scope.getEntities().removeIf(Predicate.not(this::needDump));
            if (scope instanceof ComponentScope componentScope) {
                componentScope.getInputs().replaceAll(this::repOf);
                componentScope.getOutputs().replaceAll(this::repOf);
            }
        });
    }

    public void inferAliasType() {
        for (var element : aliases.elements()) {
            var root = aliases.find(element);
            if (!element.hasImplicitType() && root.hasImplicitType()) {
                root.setType(element.getType());
            }
        }
        for (var element : aliases.elements()) {
            var root = aliases.find(element);
            if (element.hasImplicitType() && !root.hasImplicitType()) {
                element.setType(root.getType());
            }
        }
    }

    private boolean needDump(Entity entity) {
        return entity instanceof Instance inst
                && ((inst.isUserDefined() && repOf(inst) == inst) || isBuiltinRep(inst));
    }


    boolean isBuiltinRep(Instance inst) {
        return !hasSourceAlias(inst) && aliases.find(inst) == inst;
    }

    boolean hasSourceAlias(Instance inst) {
        if (aliases.contains(inst)) {
            return sourceRep.containsKey(aliases.find(inst));
        }
        return false;
    }

    @Nonnull
    Instance repOf(Instance inst) {
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
        if (inst instanceof DataAccess access) {
            var base = access.base();
            if (hasSourceAlias(base) && repOf(base) != base) {
                return DataAccess.of(repOf(base), access.selector());
            }
        }
        if (aliases.contains(inst)) {
            var result = aliases.find(inst);
            return Objects.requireNonNullElse(sourceRep.get(result), result);
        }
        return inst;
    }
}
