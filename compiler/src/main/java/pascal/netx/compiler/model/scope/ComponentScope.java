package pascal.netx.compiler.model.scope;

import pascal.netx.compiler.analysis.model.AssignManager;
import pascal.netx.compiler.elaborator.ComponentEvaluator;
import pascal.netx.compiler.model.Signature;
import pascal.netx.compiler.model.variable.Variable;
import pascal.netx.util.exception.ShallNotReachHereError;
import pascal.netx.util.lang.BuiltinComponent;
import pascal.netx.util.lang.Identifier;
import pascal.netx.util.lang.SourceLoc;

import java.util.*;

public final class ComponentScope extends Scope {
    private final boolean isInterface;
    private Identifier identifier;
    private PackageScope pack;

    private final ComponentEvaluator evaluator;
    private final Set<Alias> aliases = new LinkedHashSet<>();
    private final AssignManager manager = new AssignManager(this);

    private Attributes attributes;
    private List<Variable> inputs = new ArrayList<>();
    private List<Variable> outputs = new ArrayList<>();

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
        this.evaluator = new ComponentEvaluator(this);
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

    public boolean isInterface() {
        return isInterface;
    }

    public SourceLoc getSourceLoc() {
        return identifier.getSourceLoc();
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

    public ComponentEvaluator getEvaluator() {
        return evaluator;
    }

    public List<Variable> getInputs() {
        return inputs;
    }

    public void setInputs(List<Variable> inputs) {
        this.inputs = inputs;
    }

    public List<Variable> getOutputs() {
        return outputs;
    }

    public void setOutputs(List<Variable> outputs) {
        this.outputs = outputs;
    }

    public void addInput(Variable... inputs) {
        Collections.addAll(this.inputs, inputs);
    }

    public void addOutput(Variable... outputs) {
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

    public Set<Alias> getAliases() {
        return aliases;
    }

    public boolean isBuiltin() {
        return BuiltinComponent.isBuiltin(identifier);
    }

    public record Alias(Variable lhs, Variable rhs) {
        public Alias flip() {
            return new Alias(rhs, lhs);
        }
    }
}
