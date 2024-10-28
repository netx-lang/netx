package pascal.hardscript.compiler.model.scope;

import pascal.hardscript.compiler.ast.Identifier;
import pascal.hardscript.compiler.elaborator.Evaluator;
import pascal.hardscript.compiler.elaborator.NameAllocator;
import pascal.hardscript.compiler.model.Entity;
import pascal.hardscript.compiler.model.components.Component;
import pascal.hardscript.util.type.Pair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class Scope {
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

    public Evaluator getEvaluator() {
        return evaluator;
    }

    public NameAllocator getNameAllocator() {
        return nameAllocator;
    }

    public void alias(Component a, Component b) {
        aliases.add(new Pair<>(a, b));
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
}
