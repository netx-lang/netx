package pascal.hardscript.compiler.analysis;

import pascal.hardscript.compiler.model.Entity;
import pascal.hardscript.compiler.model.data.Data;
import pascal.hardscript.compiler.model.data.DataAccess;
import pascal.hardscript.compiler.model.data.NonValue;
import pascal.hardscript.compiler.model.data.Value;
import pascal.hardscript.compiler.model.scope.ComponentScope;
import pascal.hardscript.compiler.model.scope.DesignScope;
import pascal.hardscript.util.collection.UnionFind;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

public final class AliasAnalysis extends ModelAnalysis {
    private final UnionFind<Data> aliases = new UnionFind<>();
    private final Map<Data, Data> sourceRep = new HashMap<>();

    public AliasAnalysis(DesignScope design) {
        super(design);
    }

    @Override
    public void analyze() {
        collectAlias();
        eliminateDuplicate();
        inferAliasType();
    }

    private void collectAlias() {
        design.forEachScope(scope -> scope.getAliases()
                .forEach(pair -> pair.accept(aliases::union)));
        for (var element : aliases.elements()) {
            if (element.isUserDefined()) {
                var root = aliases.find(element);
                if (needUpdate(element, root)) {
                    sourceRep.put(root, element);
                }
            }
        }
    }

    private boolean needUpdate(Data element, Data root) {
        if (sourceRep.containsKey(root)) {
            var top = sourceRep.get(root);
            return switch (top) {
                case Value ignored -> false;
                case NonValue ignored -> {
                    if (element instanceof Value) yield true;
                    if (element.getDepth() == top.getDepth()) {
                        boolean eAccFlag = element instanceof DataAccess;
                        boolean tAccFlag = top instanceof DataAccess;
                        if (eAccFlag == tAccFlag) {
                            yield element.getSourceLoc().compareTo(top.getSourceLoc()) > 0;
                        }
                        yield tAccFlag;
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
        return entity instanceof Data data
                && ((data.isUserDefined() && repOf(data) == data) || isBuiltinRep(data));
    }


    boolean isBuiltinRep(Data data) {
        return !hasSourceAlias(data) && aliases.find(data) == data;
    }

    boolean hasSourceAlias(Data data) {
        if (aliases.contains(data)) {
            return sourceRep.containsKey(aliases.find(data));
        }
        return false;
    }

    @Nonnull
    Data repOf(Data data) {
        while (true) {
            var next = getNextRep(data);
            if (next == data) {
                return data;
            }
            data = next;
        }
    }

    @Nonnull
    private Data getNextRep(Data data) {
        if (data instanceof DataAccess access) {
            var base = access.base();
            if (hasSourceAlias(base) && repOf(base) != base) {
                return DataAccess.of(repOf(base), access.selector());
            }
        }
        if (aliases.contains(data)) {
            var result = aliases.find(data);
            return Objects.requireNonNullElse(sourceRep.get(result), result);
        }
        return data;
    }
}
