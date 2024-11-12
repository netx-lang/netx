package pascal.hardscript.compiler.model.scope;

import pascal.hardscript.compiler.elaborator.Evaluator;
import pascal.hardscript.compiler.model.Entity;
import pascal.hardscript.compiler.model.instance.Instance;
import pascal.hardscript.compiler.model.scope.binding.MapBinding;
import pascal.hardscript.util.lang.Identifier;

import java.util.*;

public abstract sealed class Scope permits BlockScope, DesignScope, ComponentScope {
    private Scope parent;
    private List<Scope> children = new ArrayList<>();
    private final MapBinding binding = MapBinding.ofEmpty();
    private final Set<Alias> aliases = new HashSet<>();

    private final Evaluator evaluator = new Evaluator(this);

    protected Scope(Scope parent) {
        this.parent = parent;
    }

    public Scope getParent() {
        return parent;
    }

    public void setParent(Scope parent) {
        this.parent = parent;
    }

    public List<Scope> getChildren() {
        return children;
    }

    public void setChildren(List<Scope> children) {
        this.children = children;
    }

    public MapBinding getBinding() {
        return binding;
    }

    public Collection<Entity> getEntities() {
        return binding.getEntities();
    }

    public Evaluator getEvaluator() {
        return evaluator;
    }

    public void addAlias(Instance a, Instance b) {
        aliases.add(Alias.of(a, b));
    }

    public Set<Alias> getAliases() {
        return aliases;
    }

    public void bind(Identifier identifier, Entity entity) {
        binding.bind(identifier, entity);
    }

    public ComponentScope instantiateComponentScope(Identifier identifier) {
        var child = new ComponentScope(this, identifier);
        children.add(child);
        return child;
    }

    public BlockScope instantiateBlockScope() {
        var child = new BlockScope(this);
        children.add(child);
        return child;
    }

    public ComponentScope getLeastUpperComponent() {
        if (this instanceof ComponentScope component) {
            return component;
        }
        return switch (parent) {
            case ComponentScope module -> module;
            case DesignScope ignored -> null;
            default -> parent.getLeastUpperComponent();
        };
    }

    public record Alias(Instance from, Instance to) {
        public static Alias of(Instance from, Instance to) {
            return new Alias(from, to);
        }
    }
}
