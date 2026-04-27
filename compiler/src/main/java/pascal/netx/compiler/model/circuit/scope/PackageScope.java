package pascal.netx.compiler.model.circuit.scope;

import pascal.netx.compiler.io.Logger;
import pascal.netx.compiler.model.ast.element.*;
import pascal.netx.compiler.model.circuit.expr.ImportRef;
import pascal.netx.compiler.model.circuit.signal.VectorValue;
import pascal.netx.util.exception.ShallNotReachHereError;
import pascal.netx.util.lang.Identifier;
import pascal.netx.util.lang.SourceLoc;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * A package scope is a container for components, interfaces, and enums.
 * A package can be either an application or a library.
 */
public final class PackageScope extends Scope {
    private final PackageDefManager defs;
    private final RepoScope project;
    private ComponentScope top;

    PackageScope(RepoScope parent) {
        super(parent);
        this.project = parent;
        this.defs = new PackageDefManager();
    }

    public static PackageScope newPackage(RepoScope parent, SourceLoc loc) {
        var result = new PackageScope(parent);
        result.setInstantiateLoc(loc);
        return result;
    }

    public ComponentScope instantiateTop(Identifier identifier) {
        if (top != null) {
            throw new ShallNotReachHereError();
        }
        top = ComponentScope.newComponent(this, identifier);
        top.setInstantiateLoc(this.instantiateLoc);
        return top;
    }

    public ComponentScope getTop() {
        return top;
    }

    public RepoScope getProject() {
        return project;
    }

    private static void doForEachSubComponent(ComponentScope scope, Consumer<ComponentScope> consumer) {
        Logger.startComponent(scope.getIdentifier(), scope.getSourceLoc());
        consumer.accept(scope);
        scope.getChildren().forEach(child -> doForEachSubComponent((ComponentScope) child, consumer));
        Logger.finishComponent();
    }

    public void forEachSubComponent(Consumer<ComponentScope> consumer) {
        doForEachSubComponent(getTop(), consumer);
    }

    public PackageDefManager defs() {
        return defs;
    }

    public static class PackageDefManager {
        private final Map<Identifier, AbstractComponentNode> componentDef = new HashMap<>();
        private final Map<Identifier, InterfaceNode> interfaceDef = new HashMap<>();
        private final Map<Identifier, ImportRef> importDef = new HashMap<>();
        private final Map<Identifier, Enum> enumDef = new HashMap<>();

        // ------- Module Information for Enum -------

        public boolean hasEnum(Identifier id) {
            return enumDef.containsKey(id);
        }

        public Enum getEnum(Identifier id) {
            return enumDef.get(id);
        }

        public void addEnum(EnumNode node) {
            enumDef.put(node.getName(), Enum.from(node));
        }

        @Nullable
        public VectorValue lookupEnum(Identifier id) {
            VectorValue result = null;
            for (var def : enumDef.values()) {
                if (def.fields().containsKey(id)) {
                    if (result != null) {
                        Logger.warn("Enum field '{}' is ambiguous", id);
                    }
                    result = def.fields().get(id);
                }
            }
            return result;
        }

        // ------- Module Information for Imported Components -------

        public boolean hasImport(Identifier id) {
            return importDef.containsKey(id);
        }

        public ImportRef getImport(Identifier id) {
            return importDef.get(id).copy(id.getSourceLoc());
        }

        public void addImport(ImportNode node) {
            var ref = node.getReference();
            var id = node.hasAlias() ? node.getAlias() : ref.getLast();
            importDef.put(id, ImportRef.of(ref));
        }

        public Collection<ImportRef> imports() {
            return importDef.values();
        }

        // ------ Module Information for Common Components ------

        public boolean hasComponent(Identifier id) {
            return componentDef.containsKey(id);
        }

        public AbstractComponentNode getComponent(Identifier id) {
            return Objects.requireNonNull(componentDef.get(id));
        }

        public void addComponent(ComponentNode node) {
            componentDef.put(node.getIdentifier(), node);
        }

        // ------ Module Information Interface Components ------

        public boolean hasInterface(Identifier id) {
            return interfaceDef.containsKey(id);
        }

        public InterfaceNode getInterface(Identifier id) {
            return interfaceDef.get(id);
        }

        public void addInterface(InterfaceNode node) {
            interfaceDef.put(node.getIdentifier(), node);
            componentDef.put(node.getIdentifier(), node);
        }
    }
}
