package pascal.netx.compiler.model.scope;

import pascal.netx.compiler.ast.element.ComponentNode;
import pascal.netx.compiler.ast.element.ImportNode;
import pascal.netx.compiler.elaborator.InterfaceElaborator;
import pascal.netx.compiler.model.variable.Bundle;
import pascal.netx.util.exception.ShallNotReachHereError;
import pascal.netx.util.lang.Identifier;

import java.util.*;
import java.util.function.Consumer;

public final class PackageScope extends Scope {
    private final Map<Identifier, Bundle.Factory> interfaceDef = new HashMap<>();
    private final Map<Identifier, ComponentNode> componentDef = new HashMap<>();
    private final Map<Identifier, List<Identifier>> importDef = new HashMap<>();
    private final ProjectScope project;

    private ComponentScope top;

    PackageScope(ProjectScope parent) {
        super(parent);
        this.project = parent;
    }

    public static PackageScope newPackage(ProjectScope parent) {
        return new PackageScope(parent);
    }

    public ComponentScope instantiateTop(Identifier identifier) {
        if (top != null) {
            throw new ShallNotReachHereError();
        }
        top = ComponentScope.newComponent(this, identifier);
        return top;
    }

    public ComponentScope getTop() {
        return top;
    }

    public ProjectScope getProject() {
        return project;
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

    // ------- Module Information for Imported Components -------

    public boolean hasImport(Identifier id) {
        return importDef.containsKey(id);
    }

    public List<Identifier> getImport(Identifier id) {
        return importDef.get(id);
    }

    public void addImport(ImportNode node) {
        if (node.hasAlias()) {
            importDef.put(node.getAlias(), node.getReference());
        } else {
            importDef.put(node.getReference().getLast(), node.getReference());
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

    public Bundle.Factory getInterface(Identifier id) {
        return interfaceDef.get(id);
    }

    public void addInterface(ComponentNode node) {
        addComponent(node);
        interfaceDef.put(node.getIdentifier(), new InterfaceElaborator(this).elaborate(node));
    }
}
