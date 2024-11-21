package pascal.hardscript.compiler.model.scope;

import pascal.hardscript.compiler.elaborator.Evaluator;
import pascal.hardscript.compiler.model.Entity;
import pascal.hardscript.compiler.model.instance.Instance;
import pascal.hardscript.compiler.model.scope.binding.MapBinding;
import pascal.hardscript.compiler.model.scope.binding.SingleBinding;
import pascal.hardscript.util.lang.Identifier;

import java.util.*;

public abstract sealed class Scope permits BlockScope, ComponentScope, DesignScope {
    private final Set<Alias> aliases = new HashSet<>();
    private final MapBinding binding = MapBinding.ofEmpty();
    private final Evaluator evaluator = new Evaluator(this);
    private List<Scope> children = new ArrayList<>();
    private Scope parent;

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

    public void addChild(Scope child) {
        children.add(child);
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

    public void addAlias(Instance lhs, Instance rhs) {
        aliases.add(Alias.of(lhs, rhs));
    }

    public Set<Alias> getAliases() {
        return aliases;
    }

    public void bind(SingleBinding single) {
        binding.bind(single.key(), single.value());
    }

    public void bind(Identifier identifier, Entity entity) {
        binding.bind(identifier, entity);
    }

    public ComponentScope instantiateComponentScope(Identifier identifier) {
        var child = new ComponentScope(this, identifier);
        addChild(child);
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
            case null -> null;
            case ComponentScope module -> module;
            default -> parent.getLeastUpperComponent();
        };
    }

    public record Alias(Instance lhs, Instance rhs) {
        public static Alias of(Instance lhs, Instance rhs) {
            return new Alias(lhs, rhs);
        }
    }
}
