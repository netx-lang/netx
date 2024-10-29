package pascal.hardscript.compiler.analysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pascal.hardscript.compiler.model.components.Component;
import pascal.hardscript.compiler.model.scope.DesignScope;
import pascal.hardscript.util.collection.UnionFind;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AliasAnalysis extends ModelAnalysis<AliasAnalysis.Result> {
    private static final Logger log = LoggerFactory.getLogger(AliasAnalysis.class);
    private final UnionFind<Component> aliases = new UnionFind<>();

    public AliasAnalysis(DesignScope design) {
        super(design);
    }

    @Override
    public Result analyze() {
        Map<Component, Component> representatives = new HashMap<>();
        design.forEachScope(scope -> scope.getAliases().forEach(pair -> pair.accept(aliases::union)));
        for (var element : aliases.elements()) {
            if (element.isUserDefined()) {
                var root = aliases.find(element);
                if (representatives.containsKey(root)) {
                    log.warn("Component {} is aliased to {}", element, representatives.get(root));
                } else {
                    representatives.put(root, element);
                }
            }
        }
        return new Result(aliases, representatives);
    }

    public static class Result {
        private final UnionFind<Component> aliases;
        private final Map<Component, Component> representatives;

        public Result(UnionFind<Component> aliases,
                      Map<Component, Component> representatives) {
            this.aliases = aliases;
            this.representatives = representatives;
        }

        @Nullable
        public Component sourceRepOf(Component component) {
            return representatives.get(aliases.find(component));
        }

        @Nonnull
        public Component repOf(Component component) {
            var root = aliases.find(component);
            return Objects.requireNonNullElse(representatives.get(root), root);
        }
    }
}
