package pascal.netx.compiler.model.circuit.scope;

import pascal.netx.compiler.model.circuit.Entity;
import pascal.netx.compiler.model.circuit.SystemMeta;
import pascal.netx.compiler.model.circuit.expr.BuiltinRef;
import pascal.netx.compiler.model.circuit.scope.binding.Binding;
import pascal.netx.compiler.model.circuit.scope.binding.IdEntityMap;
import pascal.netx.util.lang.BuiltinComponent;
import pascal.netx.util.lang.Identifier;
import pascal.netx.util.lang.SourceLoc;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public abstract sealed class Scope permits ComponentScope, PackageScope, RepoScope {
    private LinkedList<Binding> bindings;
    private boolean lexicalBindings;
    private final List<Scope> children;
    private Scope parent;
    protected SourceLoc instantiateLoc;

    protected Scope(Scope parent) {
        this.parent = parent;
        this.children = new ArrayList<>();
        this.bindings = new LinkedList<>();
        this.bindings.add(IdEntityMap.ofEmpty());
    }

    public Scope getParent() {
        return parent;
    }

    public void setParent(Scope parent) {
        this.parent.children.removeIf(child -> child == this);
        parent.addChild(this);
        this.parent = parent;
    }

    public void setInstantiateLoc(SourceLoc instantiateLoc) {
        this.instantiateLoc = instantiateLoc;
    }

    public List<Scope> getChildren() {
        return children;
    }

    public void addChild(Scope child) {
        children.add(child);
    }

    public void mergeBindingsFrom(Scope scope) {
        for (var binding : scope.bindings) {
            overrideBindings(binding);
        }
    }

    public void forEachEntity(Consumer<Entity> consumer) {
        Set<Entity> visited = new HashSet<>();
        for (var binding : bindings) {
            binding.forEachEntity(entity -> {
                if (visited.add(entity)) {
                    consumer.accept(entity);
                }
            });
        }
    }

    public void releaseEntityIf(Predicate<Entity> predicate) {
        for (var binding : bindings) {
            binding.releaseIf(predicate);
        }
    }

    public Binding getBaseBinding() {
        return bindings.getLast();
    }

    public void bind(Identifier identifier, Entity entity) {
        getBaseBinding().bind(identifier, entity);
    }

    public void bind(Map.Entry<Identifier, Entity> entry) {
        bind(entry.getKey(), entry.getValue());
    }

    public void overrideBindings(Binding binding) {
        bindings.addFirst(binding);
    }

    public void restoreBindings() {
        bindings.removeFirst();
    }

    /** Isolate lexical lookup while keeping the active circuit construction scope. */
    public <T> T withLexicalBindings(Binding binding, Supplier<T> action) {
        var previous = bindings;
        var previousLexical = lexicalBindings;
        bindings = new LinkedList<>();
        bindings.add(binding);
        lexicalBindings = true;
        try {
            return action.get();
        } finally {
            bindings = previous;
            lexicalBindings = previousLexical;
        }
    }

    protected boolean hasLexicalBindings() {
        return lexicalBindings;
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
        if (lexicalBindings) {
            return null;
        }
        if (BuiltinComponent.isBuiltin(id)) {
            return BuiltinRef.of(BuiltinComponent.of(id), id.getSourceLoc());
        }
        return SystemMeta.resolve(id);
    }
}
