package pascal.hardscript.compiler.model.scope;

import pascal.hardscript.compiler.elaborator.Evaluator;
import pascal.hardscript.compiler.model.Entity;
import pascal.hardscript.compiler.model.instance.Instance;
import pascal.hardscript.compiler.model.scope.binding.MapBinding;
import pascal.hardscript.compiler.model.scope.binding.SingleBinding;
import pascal.hardscript.util.lang.Identifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract sealed class Scope permits ComponentScope, GlobalScope {
    private final MapBinding binding = MapBinding.ofEmpty();
    private final List<ComponentScope> children;
    private final Evaluator evaluator;
    private final GlobalScope global;

    protected Scope(GlobalScope global) {
        this.global = global;
        this.children = new ArrayList<>();
        this.evaluator = new Evaluator(this);
    }

    public GlobalScope getGlobal() {
        return global;
    }

    public List<ComponentScope> getChildren() {
        return children;
    }

    public void addChild(ComponentScope child) {
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

    public void bind(SingleBinding single) {
        binding.bind(single.key(), single.value());
    }

    public void bind(Identifier identifier, Entity entity) {
        binding.bind(identifier, entity);
    }

    abstract public ComponentScope instantiateComponentScope(Identifier identifier);

    public record Alias(Instance lhs, Instance rhs) {
        public static Alias of(Instance lhs, Instance rhs) {
            return new Alias(lhs, rhs);
        }
    }
}
