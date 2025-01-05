package pascal.netx.compiler.model.scope;

import pascal.netx.compiler.model.Entity;
import pascal.netx.compiler.model.scope.binding.Binding;
import pascal.netx.compiler.model.scope.binding.MonoBinding;
import pascal.netx.compiler.model.scope.binding.MultiBinding;
import pascal.netx.util.exception.ShallNotReachHereError;
import pascal.netx.util.lang.Identifier;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public abstract sealed class Scope permits ComponentScope, PackageScope, ProjectScope {
    private final Scope parent;
    private final LinkedList<Binding> bindings;
    private final List<Scope> children;

    protected Scope(Scope parent) {
        this.parent = parent;
        this.children = new ArrayList<>();
        this.bindings = new LinkedList<>();
        this.bindings.add(MultiBinding.ofEmpty());
    }

    public Scope getParent() {
        return parent;
    }

    public List<Scope> getChildren() {
        return children;
    }

    public void addChild(Scope child) {
        children.add(child);
    }

    public Binding getBinding() {
        if (bindings.size() != 1) {
            throw new ShallNotReachHereError("Scope has overridden bindings");
        }
        return bindings.getFirst();
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
}
