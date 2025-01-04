package pascal.netx.compiler.model.scope;

import pascal.netx.compiler.model.Entity;
import pascal.netx.compiler.model.instance.Instance;
import pascal.netx.compiler.model.scope.binding.Binding;
import pascal.netx.compiler.model.scope.binding.MonoBinding;
import pascal.netx.compiler.model.scope.binding.MultiBinding;
import pascal.netx.util.lang.Identifier;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public abstract sealed class Scope permits ComponentScope, ModuleScope {
    private final LinkedList<Binding> bindings;
    private final List<ComponentScope> children;

    protected Scope() {
        this.children = new ArrayList<>();
        this.bindings = new LinkedList<>();
        this.bindings.add(MultiBinding.ofEmpty());
    }

    public List<ComponentScope> getChildren() {
        return children;
    }

    public void addChild(ComponentScope child) {
        children.add(child);
    }

    public Binding getBinding() {
        return bindings.getLast();
    }

    public void bind(MonoBinding single) {
        getBinding().bind(single);
    }

    public void bind(Identifier identifier, Entity entity) {
        getBinding().bind(identifier, entity);
    }

    public void overrideBindings(Binding binding) {
        bindings.addFirst(binding);
    }

    public void restoreBindings() {
        bindings.removeFirst();
    }

    @Nullable
    public Entity lookup(Identifier id) {
        for (var binding : bindings) {
            var entity = binding.lookup(id);
            if (entity != null) {
                return entity;
            }
        }
        return null;
    }

    abstract public ComponentScope instantiateComponentScope(Identifier identifier);

    abstract public ComponentScope instantiateInterfaceScope(Identifier identifier);

    public record Alias(Instance lhs, Instance rhs) {
        public static Alias of(Instance lhs, Instance rhs) {
            return new Alias(lhs, rhs);
        }
    }
}
