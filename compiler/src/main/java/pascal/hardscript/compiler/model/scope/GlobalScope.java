package pascal.hardscript.compiler.model.scope;

import pascal.hardscript.compiler.ast.component.ComponentNode;
import pascal.hardscript.compiler.elaborator.BuiltInComponentFactory;
import pascal.hardscript.compiler.model.instance.DataBundle;
import pascal.hardscript.util.exception.ShallNotReachHereError;
import pascal.hardscript.util.lang.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class GlobalScope extends Scope {
    private final Map<Identifier, DataBundle.Factory> interfaceDef = new HashMap<>();
    private final Map<Identifier, ComponentNode> componentDef = new HashMap<>();
    private final BuiltInComponentFactory factory;
    private final Supplier<Integer> dataIndexer;
    private ComponentScope top;

    GlobalScope() {
        super(null);
        this.dataIndexer = new AtomicInteger(0)::incrementAndGet;
        this.factory = new BuiltInComponentFactory(dataIndexer);
    }

    public static GlobalScope newDesign() {
        return new GlobalScope();
    }

    public ComponentScope instantiateTop(Identifier identifier) {
        if (this.top != null) {
            throw new ShallNotReachHereError();
        }
        this.top = instantiateComponentScope(identifier);
        return this.top;
    }

    public ComponentScope getTop() {
        return top;
    }

    public Supplier<Integer> getDataIndexer() {
        return dataIndexer;
    }

    public BuiltInComponentFactory getBuiltinFactory() {
        return factory;
    }

    public boolean hasComponent(Identifier id) {
        return componentDef.containsKey(id);
    }

    public ComponentNode getComponent(Identifier id) {
        return Objects.requireNonNull(componentDef.get(id));
    }

    public void addComponent(ComponentNode node) {
        componentDef.put(node.getIdentifier(), node);
    }

    public boolean hasInterface(Identifier id) {
        return interfaceDef.containsKey(id);
    }

    public DataBundle.Factory getInterface(Identifier id) {
        return interfaceDef.get(id);
    }

    public void addInterface(Identifier id, DataBundle.Factory factory) {
        interfaceDef.put(id, factory);
    }

    public void forEachSubScope(Consumer<ComponentScope> consumer) {
        Stack<ComponentScope> stack = new Stack<>();
        stack.push(getTop());
        while (!stack.isEmpty()) {
            ComponentScope scope = stack.pop();
            consumer.accept(scope);
            stack.addAll(scope.getChildren());
        }
    }

    @Override
    public ComponentScope instantiateComponentScope(Identifier identifier) {
        var child = new ComponentScope(this, false, identifier);
        addChild(child);
        return child;
    }

    @Override
    public ComponentScope instantiateInterfaceScope(Identifier identifier) {
        var child = new ComponentScope(this, true, identifier);
        addChild(child);
        return child;
    }
}
