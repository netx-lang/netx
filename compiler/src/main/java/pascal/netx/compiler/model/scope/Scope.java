package pascal.netx.compiler.model.scope;

import pascal.netx.compiler.model.Entity;
import pascal.netx.compiler.model.scope.binding.Binding;
import pascal.netx.compiler.model.scope.binding.MultiBinding;
import pascal.netx.util.lang.Identifier;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public abstract sealed class Scope permits ComponentScope, PackageScope, RepoScope {
    private final LinkedList<Binding> bindings;
    private final List<Scope> children;
    private Scope parent;

    protected Scope(Scope parent) {
        this.parent = parent;
        this.children = new ArrayList<>();
        this.bindings = new LinkedList<>();
        this.bindings.add(MultiBinding.ofEmpty());
    }

    public Scope getParent() {
        return parent;
    }

    public void setParent(Scope parent) {
        this.parent.children.removeIf(child -> child == this);
        parent.addChild(this);
        this.parent = parent;
    }

    public List<Scope> getChildren() {
        return children;
    }

    public void addChild(Scope child) {
        children.add(child);
    }

    public Binding getBinding() {
        return bindings.getLast();
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

    public int getBindingCount() {
        return bindings.size();
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
