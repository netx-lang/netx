package pascal.netx.compiler.model.scope;

import pascal.netx.compiler.ast.element.ComponentNode;
import pascal.netx.compiler.ast.element.ImportNode;
import pascal.netx.compiler.elaborator.BuiltInComponentFactory;
import pascal.netx.compiler.elaborator.InterfaceElaborator;
import pascal.netx.compiler.model.instance.DataBundle;
import pascal.netx.util.exception.ShallNotReachHereError;
import pascal.netx.util.lang.Identifier;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class PackageScope extends Scope {
    private final Map<Identifier, DataBundle.Factory> interfaceDef = new HashMap<>();
    private final Map<Identifier, ComponentNode> componentDef = new HashMap<>();
    private final Map<Identifier, List<Identifier>> importRef = new HashMap<>();
    private final BuiltInComponentFactory factory;
    private Supplier<Integer> dataIndexer;
    private ComponentScope top;

    PackageScope(Scope parent) {
        super(parent);
        this.dataIndexer = new AtomicInteger(0)::incrementAndGet;
        this.factory = new BuiltInComponentFactory(this);
    }

    public static PackageScope newPackage(ProjectScope parent) {
        return new PackageScope(parent);
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

    public void forEachSubComponent(Consumer<ComponentScope> consumer) {
        Stack<ComponentScope> stack = new Stack<>();
        stack.push(getTop());
        while (!stack.isEmpty()) {
            ComponentScope scope = stack.pop();
            consumer.accept(scope);
            scope.getChildren().forEach(child -> stack.push((ComponentScope) child));
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

    public void addImportRef(ImportNode node) {
        if (node.hasAlias()) {
            importRef.put(node.getAlias(), node.getReference());
        } else {
            importRef.put(node.getReference().getLast(), node.getReference());
        }
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
