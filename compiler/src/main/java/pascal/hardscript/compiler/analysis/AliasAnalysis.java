package pascal.hardscript.compiler.analysis;

import pascal.hardscript.compiler.model.data.Data;
import pascal.hardscript.compiler.model.data.DataAccess;
import pascal.hardscript.compiler.model.scope.DesignScope;
import pascal.hardscript.util.collection.UnionFind;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AliasAnalysis extends ModelAnalysis<AliasAnalysis.Result> {
    private final UnionFind<Data> aliases = new UnionFind<>();
    private final Map<Data, Data> representatives = new HashMap<>();

    public AliasAnalysis(DesignScope design) {
        super(design);
    }

    @Override
    public Result analyze() {
        design.forEachScope(scope -> scope.getAliases()
                .forEach(pair -> pair.accept(aliases::union)));
        for (var element : aliases.elements()) {
            if (element.isUserDefined()) {
                var root = aliases.find(element);
                if (needUpdate(element, root)) {
                    representatives.put(root, element);
                }
            }
        }
        return new Result(aliases, representatives);
    }

    private boolean needUpdate(Data element, Data root) {
        if (representatives.containsKey(root)) {
            var top = representatives.get(root);
            if (element.getDepth() == top.getDepth()) {
                if (!(element instanceof DataAccess) && top instanceof DataAccess) {
                    return true;
                }
                return element.getSourceLoc().compareTo(top.getSourceLoc()) > 0;
            }
            return element.getDepth() < top.getDepth();
        }
        return true;
    }

    public static class Result {
        private final UnionFind<Data> aliases;
        private final Map<Data, Data> sourceRep;

        public Result(UnionFind<Data> aliases,
                      Map<Data, Data> sourceRep) {
            this.aliases = aliases;
            this.sourceRep = sourceRep;
        }

        public boolean isBuiltinRep(Data data) {
            return !hasSourceAlias(data) && aliases.find(data) == data;
        }

        public boolean hasSourceAlias(Data data) {
            if (aliases.contains(data)) {
                return sourceRep.containsKey(aliases.find(data));
            }
            return false;
        }

        @Nonnull
        public Data repOf(Data data) {
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
}
