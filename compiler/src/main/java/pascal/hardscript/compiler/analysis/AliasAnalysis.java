package pascal.hardscript.compiler.analysis;

import pascal.hardscript.compiler.model.components.Component;
import pascal.hardscript.compiler.model.components.ComponentAccess;
import pascal.hardscript.compiler.model.scope.DesignScope;
import pascal.hardscript.util.collection.UnionFind;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AliasAnalysis extends ModelAnalysis<AliasAnalysis.Result> {
    private final UnionFind<Component> aliases = new UnionFind<>();
    private final Map<Component, Component> representatives = new HashMap<>();

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

    private boolean needUpdate(Component element, Component root) {
        if (representatives.containsKey(root)) {
            // replace when there is a higher level component
            var top = representatives.get(root);
            if (element.getDepth() < top.getDepth()) {
                return true;
            }
            // ensure there is a fixed point
            return !(element instanceof ComponentAccess) && top instanceof ComponentAccess;
        }
        return true;
    }

    public static class Result {
        private final UnionFind<Component> aliases;
        private final Map<Component, Component> sourceRep;

        public Result(UnionFind<Component> aliases,
                      Map<Component, Component> sourceRep) {
            this.aliases = aliases;
            this.sourceRep = sourceRep;
        }

        public boolean hasSourceAlias(Component component) {
            if (aliases.contains(component)) {
                return sourceRep.containsKey(aliases.find(component));
            }
            return false;
        }

        @Nonnull
        public Component repOf(Component component) {
            while (true) {
                var next = getNextRep(component);
                if (next == component) {
                    return component;
                }
                component = next;
            }
        }

        @Nonnull
        private Component getNextRep(Component component) {
            if (component instanceof ComponentAccess access) {
                var base = access.base();
                if (hasSourceAlias(base) && repOf(base) != base) {
                    return ComponentAccess.of(repOf(base), access.selector());
                }
            }
            if (aliases.contains(component)) {
                var result = aliases.find(component);
                return Objects.requireNonNullElse(sourceRep.get(result), result);
            }
            return component;
        }

    }
}
