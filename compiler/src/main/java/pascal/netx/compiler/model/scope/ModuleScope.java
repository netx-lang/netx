package pascal.netx.compiler.model.scope;

import pascal.netx.compiler.ast.element.ComponentNode;
import pascal.netx.compiler.elaborator.BuiltInComponentFactory;
import pascal.netx.compiler.elaborator.InterfaceElaborator;
import pascal.netx.compiler.model.instance.DataBundle;
import pascal.netx.util.exception.ShallNotReachHereError;
import pascal.netx.util.lang.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class ModuleScope extends Scope {
    private final Map<Identifier, DataBundle.Factory> interfaceDef = new HashMap<>();
    private final Map<Identifier, ComponentNode> componentDef = new HashMap<>();
    private final BuiltInComponentFactory factory;
    private Supplier<Integer> dataIndexer;
    private ComponentScope top;

    ModuleScope(Scope parent) {
        super(parent);
        this.dataIndexer = new AtomicInteger(0)::incrementAndGet;
        this.factory = new BuiltInComponentFactory(this);
    }

    public static ModuleScope newModule() {
        // TODO: specify parent
        return new ModuleScope(null);
    }

    public ComponentScope instantiateTop(Identifier identifier) {
        if (top != null) {
            throw new ShallNotReachHereError();
        }
        top = ComponentScope.newComponent(this, identifier);
        addChild(top);
        return top;
    }

    public ComponentScope getTop() {
        return top;
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

    public Supplier<Integer> getDataIndexer() {
        return dataIndexer;
    }

    public void resetDataIndexer() {
        dataIndexer = new AtomicInteger(0)::incrementAndGet;
    }

    public BuiltInComponentFactory getBuiltinFactory() {
        return factory;
    }

    // ------ Module Information for Common Components ------

    public boolean hasComponent(Identifier id) {
        return componentDef.containsKey(id);
    }

    public ComponentNode getComponent(Identifier id) {
        return Objects.requireNonNull(componentDef.get(id));
    }

    public void addComponent(ComponentNode node) {
        componentDef.put(node.getIdentifier(), node);
    }

    // ------ Module Information Interface Components ------

    public boolean hasInterface(Identifier id) {
        return interfaceDef.containsKey(id);
    }

    public DataBundle.Factory getInterface(Identifier id) {
        return interfaceDef.get(id);
    }

    public void addInterface(ComponentNode node) {
        addComponent(node);
        interfaceDef.put(node.getIdentifier(), new InterfaceElaborator(this).elaborate(node));
    }
}
