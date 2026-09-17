package pascal.netx.compiler.model.circuit.scope;

import pascal.netx.compiler.backend.Evaluator;
import pascal.netx.compiler.backend.analysis.circuit.AssignManager;
import pascal.netx.compiler.io.Logger;
import pascal.netx.compiler.model.circuit.Entity;
import pascal.netx.compiler.model.circuit.Signature;
import pascal.netx.compiler.model.circuit.expr.ComponentRef;
import pascal.netx.compiler.model.circuit.signal.Signal;
import pascal.netx.compiler.model.circuit.signal.VectorValue;
import pascal.netx.util.exception.ShallNotReachHereError;
import pascal.netx.util.lang.BuiltinComponent;
import pascal.netx.util.lang.Identifier;
import pascal.netx.util.lang.SourceLoc;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public final class ComponentScope extends Scope {
    private final boolean isInterface;
    private Identifier identifier;
    private PackageScope pack;

    private final Evaluator evaluator;
    private final List<Alias> aliases = new LinkedList<>();
    private final AssignManager manager = new AssignManager(this);

    private Attributes attributes;
    private List<Signal> inputs = new ArrayList<>();
    private List<Signal> outputs = new ArrayList<>();

    /**
     * Only used in compact compilation mode to distinguish the components.
     * In expanded mode, the signature is null since every instance is unique.
     */
    private Signature signature;

    public ComponentScope(Scope parent, boolean isInterface, Identifier identifier) {
        super(parent);
        this.pack = resolvePackage(parent);
        this.isInterface = isInterface;
        this.identifier = identifier;
        this.evaluator = new Evaluator(this);
    }

    private static PackageScope resolvePackage(Scope scope) {
        return switch (scope) {
            case PackageScope pack -> pack;
            case ComponentScope component -> resolvePackage(component.getParent());
            default -> throw new ShallNotReachHereError();
        };
    }

    public static ComponentScope newComponent(Scope parent, Identifier identifier) {
        var result = new ComponentScope(parent, false, identifier);
        parent.addChild(result);
        return result;
    }

    public static ComponentScope newInterface(Scope parent, Identifier identifier, boolean asChild) {
        var result = new ComponentScope(parent, true, identifier);
        if (asChild) {
            parent.addChild(result);
        }
        return result;
    }

    @Nullable
    public Entity resolveIdentifier(Identifier id) {
        Entity entity = lookup(id);
        if (entity == null && !hasLexicalBindings()) {
            entity = resolveIdentifierInPack(id);
        }
        return entity;
    }

    @Nullable
    private Entity resolveIdentifierInPack(Identifier id) {
        Entity entity = pack.lookup(id);
        if (entity == null) {
            if (pack.defs().hasComponent(id)) {
                entity = ComponentRef.of(pack.defs().getComponent(id), id);
            } else if (pack.defs().hasImport(id)) {
                entity = pack.defs().getImport(id);
            } else if (pack.defs().hasEnum(id)) {
                entity = pack.defs().getEnum(id);
            } else {
                entity = pack.defs().lookupEnum(id);
                if (entity == null) {
                    entity = lookupImportedEnumField(id);
                }
            }
        }

        // Handling cross-package resolution
        if (entity == null && getParent() instanceof ComponentScope parent) {
            entity = parent.resolveIdentifierInPack(id);
        }
        return entity;
    }

    @Nullable
    private VectorValue lookupImportedEnumField(Identifier id) {
        VectorValue result = null;
        var manager = pack.getProject().getManager();
        for (var ref : pack.defs().imports()) {
            var importedEnum = manager.resolveImportedEnum(ref);
            if (importedEnum != null && importedEnum.fields().containsKey(id)) {
                if (result != null) {
                    Logger.warn("Enum field '{}' is ambiguous", id);
                }
                result = importedEnum.fields().get(id);
            }
        }
        return result;
    }

    public boolean isInterface() {
        return isInterface;
    }

    public SourceLoc getSourceLoc() {
        return instantiateLoc;
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public void setIdentifier(Identifier identifier) {
        this.identifier = identifier;
    }

    public PackageScope getPackage() {
        return pack;
    }

    public void setPackage(PackageScope pack) {
        this.pack = pack;
    }

    public Evaluator getEvaluator() {
        return evaluator;
    }

    public List<Signal> getInputs() {
        return inputs;
    }

    public void setInputs(List<Signal> inputs) {
        this.inputs = inputs;
    }

    public List<Signal> getOutputs() {
        return outputs;
    }

    public void setOutputs(List<Signal> outputs) {
        this.outputs = outputs;
    }

    public void addInput(Signal... inputs) {
        Collections.addAll(this.inputs, inputs);
    }

    public void addOutput(Signal... outputs) {
        Collections.addAll(this.outputs, outputs);
    }

    public Signature getSignature() {
        return signature;
    }

    public void setSignature(Signature signature) {
        this.signature = signature;
    }

    public boolean hasAttributes() {
        return attributes != null && !attributes.isEmpty();
    }

    public Attributes getAttributes() {
        return attributes;
    }

    public void setAttributes(Attributes attributes) {
        this.attributes = attributes;
    }

    public AssignManager assigns() {
        return manager;
    }

    public List<Alias> getAliases() {
        return aliases;
    }

    public boolean isBuiltin() {
        return BuiltinComponent.isBuiltin(identifier);
    }

    public BuiltinComponent asBuiltin() {
        if (!isBuiltin()) {
            throw new ShallNotReachHereError();
        }
        return BuiltinComponent.of(identifier);
    }

    public record Alias(Signal lhs, Signal rhs) {
        public Alias flip() {
            return new Alias(rhs, lhs);
        }
    }
}
