package pascal.hardscript.compiler.model.scope;

import pascal.hardscript.compiler.ast.Identifier;
import pascal.hardscript.compiler.elaborator.Evaluator;
import pascal.hardscript.compiler.model.Entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Scope {
    private final Scope parent;
    private final Evaluator evaluator = new Evaluator(this);
    private final List<Scope> children = new ArrayList<>();
    private final Map<Identifier, Entity> binding = new HashMap<>();

    protected Scope(Scope parent) {
        this.parent = parent;
    }

    public Scope getParent() {
        return parent;
    }

    public Evaluator getEvaluator() {
        return evaluator;
    }

    public void bind(Identifier identifier, Entity entity) {
        binding.put(identifier, entity);
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
