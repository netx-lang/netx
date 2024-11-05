package pascal.hardscript.compiler.model.scope;

import pascal.hardscript.compiler.ast.Identifier;
import pascal.hardscript.compiler.elaborator.Evaluator;
import pascal.hardscript.compiler.elaborator.NameAllocator;
import pascal.hardscript.compiler.model.Entity;
import pascal.hardscript.compiler.model.instance.Instance;
import pascal.hardscript.util.type.Pair;

import java.util.*;
import java.util.function.Consumer;

public abstract sealed class Scope permits BlockScope, DesignScope, ComponentScope {
    private final Scope parent;
    private final MapBinding binding = new MapBinding();
    private final List<Scope> children = new ArrayList<>();
    private final NameAllocator nameAllocator = new NameAllocator();
    private final Set<Pair<Instance, Instance>> aliases = new HashSet<>();

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

    public void alias(Instance a, Instance b) {
        aliases.add(new Pair<>(a, b));
    }

    public Set<Pair<Instance, Instance>> getAliases() {
        return Collections.unmodifiableSet(aliases);
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

    public ComponentScope getUpperComponent() {
        return switch (parent) {
            case ComponentScope module -> module;
            case DesignScope ignored -> null;
            default -> parent.getUpperComponent();
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
