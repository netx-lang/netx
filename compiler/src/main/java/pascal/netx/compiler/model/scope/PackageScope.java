package pascal.netx.compiler.model.scope;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pascal.netx.compiler.ast.element.ComponentNode;
import pascal.netx.compiler.ast.element.EnumNode;
import pascal.netx.compiler.ast.element.ImportNode;
import pascal.netx.compiler.elaborator.InterfaceElaborator;
import pascal.netx.compiler.model.expr.ImportRef;
import pascal.netx.compiler.model.variable.BitVectorValue;
import pascal.netx.compiler.model.variable.Bundle;
import pascal.netx.util.exception.ShallNotReachHereError;
import pascal.netx.util.lang.BitVector;
import pascal.netx.util.lang.Identifier;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;
import java.util.function.Consumer;

/**
 * A package scope is a container for components, interfaces, and enums.
 * A package can be either an application or a library.
 */
public final class PackageScope extends Scope {
    private static final Logger log = LoggerFactory.getLogger(PackageScope.class);

    private final PackageDefManager defs;
    private final RepoScope project;
    private ComponentScope top;

    PackageScope(RepoScope parent) {
        super(parent);
        this.project = parent;
        this.defs = new PackageDefManager();
    }

    public static PackageScope newPackage(RepoScope parent) {
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

    public RepoScope getProject() {
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

    public PackageDefManager defs() {
        return defs;
    }

    public class PackageDefManager {
        private final Map<Identifier, Bundle.Factory> interfaceDef = new HashMap<>();
        private final Map<Identifier, ComponentNode> componentDef = new HashMap<>();
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
            Map<Identifier, BitVectorValue> enumMap = new HashMap<>();
            int width = BigInteger.valueOf(node.getFields().size() - 1).bitLength();
            for (int i = 0; i < node.getFields().size(); i++) {
                enumMap.put(node.getFields().get(i), BitVectorValue.of(BitVector.fromInt(i, width)));
            }
            enumDef.put(node.getName(), new Enum(node.getName(), width, enumMap));
        }

        public BitVectorValue lookupEnum(Identifier id) {
            BitVectorValue result = null;
            for (var def : enumDef.values()) {
                if (def.fields().containsKey(id)) {
                    if (result != null) {
                        log.warn("Enum field '{}' is ambiguous", id);
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
            return importDef.get(id).copy();
        }

        public void addImport(ImportNode node) {
            var ref = node.getReference();
            var id = node.hasAlias() ? node.getAlias() : ref.getLast();
            importDef.put(id, ImportRef.of(ref, node.getSourceLoc()));
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
            interfaceDef.put(node.getIdentifier(), new InterfaceElaborator(PackageScope.this).elaborate(node));
        }
    }
}
