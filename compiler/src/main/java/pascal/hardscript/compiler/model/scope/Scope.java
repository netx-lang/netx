package pascal.hardscript.compiler.model.scope;

import pascal.hardscript.compiler.ast.Identifier;
import pascal.hardscript.compiler.elaborator.Evaluator;
import pascal.hardscript.compiler.model.Entity;

import java.util.ArrayList;
import java.util.List;

public abstract class Scope {
    private final Scope parent;
    private final Binding binding = new Binding();
    private final List<Scope> children = new ArrayList<>();

    private final Evaluator evaluator = new Evaluator(this);

    protected Scope(Scope parent) {
        this.parent = parent;
    }

    public Scope getParent() {
        return parent;
    }

    public Binding getBinding() {
        return binding;
    }

    public Evaluator getEvaluator() {
        return evaluator;
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
