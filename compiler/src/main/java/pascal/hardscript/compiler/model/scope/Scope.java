package pascal.hardscript.compiler.model.scope;

import pascal.hardscript.compiler.ast.Identifier;
import pascal.hardscript.compiler.elaborator.Evaluator;
import pascal.hardscript.compiler.elaborator.NameAllocator;
import pascal.hardscript.compiler.model.Entity;
import pascal.hardscript.compiler.model.components.Component;
import pascal.hardscript.util.type.Pair;

import java.util.*;
import java.util.function.Consumer;

public abstract sealed class Scope permits BlockScope, DesignScope, ModuleScope {
    private final Scope parent;
    private final MapBinding binding = new MapBinding();
    private final List<Scope> children = new ArrayList<>();
    private final NameAllocator nameAllocator = new NameAllocator();
    private final Set<Pair<Component, Component>> aliases = new HashSet<>();

    private final Evaluator evaluator = new Evaluator(this);

    protected Scope(Scope parent) {
        this.parent = parent;
    }

    public Scope getParent() {
        return parent;
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

    public NameAllocator getNameAllocator() {
        return nameAllocator;
    }

    public List<Scope> getChildren() {
        return children;
    }

    public void alias(Component a, Component b) {
        aliases.add(new Pair<>(a, b));
    }

    public Set<Pair<Component, Component>> getAliases() {
        return Collections.unmodifiableSet(aliases);
    }

    public void bind(Identifier identifier, Entity entity) {
        binding.bind(identifier, entity);
    }

    public ModuleScope instantiateModuleScope(Identifier identifier) {
        var child = new ModuleScope(this, identifier);
        children.add(child);
        return child;
    }

    public BlockScope instantiateBlockScope() {
        var child = new BlockScope(this);
        children.add(child);
        return child;
    }

    public ModuleScope getUpperModule() {
        return switch (parent) {
            case ModuleScope module -> module;
            case DesignScope ignored -> null;
            default -> parent.getUpperModule();
        };
    }

    public void forEachScope(Consumer<Scope> consumer) {
        Stack<Scope> stack = new Stack<>();
        stack.push(this);
        while (!stack.isEmpty()) {
            Scope scope = stack.pop();
            consumer.accept(scope);
            stack.addAll(scope.getChildren());
        }
    }
}
