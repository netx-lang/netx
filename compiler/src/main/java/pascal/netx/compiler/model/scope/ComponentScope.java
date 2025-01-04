package pascal.netx.compiler.model.scope;

import pascal.netx.compiler.analysis.AssignManager;
import pascal.netx.compiler.elaborator.ComponentEvaluator;
import pascal.netx.compiler.model.component.Signature;
import pascal.netx.compiler.model.instance.Instance;
import pascal.netx.util.exception.ShallNotReachHereError;
import pascal.netx.util.lang.BuiltinComponent;
import pascal.netx.util.lang.Identifier;
import pascal.netx.util.lang.SourceLoc;

import java.util.*;

public final class ComponentScope extends Scope {
    private final ModuleScope module;
    private final boolean isInterface;
    private final Identifier identifier;

    private final ComponentEvaluator evaluator;
    private final Set<Alias> aliases = new HashSet<>();
    private final AssignManager manager = new AssignManager(this);

    private Attributes attributes;
    private List<Instance> inputs = new ArrayList<>();
    private List<Instance> outputs = new ArrayList<>();

    /**
     * Only used in compact compilation mode to distinguish the components.
     * In expanded mode, the signature is null since every instance is unique.
     */
    private Signature signature;

    ComponentScope(Scope parent, boolean isInterface, Identifier identifier) {
        super(parent);
        this.module = switch (parent) {
            case ModuleScope scope -> scope;
            case ComponentScope scope -> scope.getModule();
            default -> throw new ShallNotReachHereError();
        };
        this.isInterface = isInterface;
        this.identifier = identifier;
        this.evaluator = new ComponentEvaluator(this);
    }

    public static ComponentScope newComponent(Scope parent, Identifier identifier) {
        var result = new ComponentScope(parent, false, identifier);
        parent.addChild(result);
        return result;
    }

    public static ComponentScope newInterface(Scope parent, Identifier identifier) {
        var result = new ComponentScope(parent, true, identifier);
        parent.addChild(result);
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

    public ModuleScope getModule() {
        return module;
    }

    public ComponentEvaluator getEvaluator() {
        return evaluator;
    }

    public List<Instance> getInputs() {
        return inputs;
    }

    public void setInputs(List<Instance> inputs) {
        this.inputs = inputs;
    }

    public List<Instance> getOutputs() {
        return outputs;
    }

    public void setOutputs(List<Instance> outputs) {
        this.outputs = outputs;
    }

    public void addInput(Instance... inputs) {
        Collections.addAll(this.inputs, inputs);
    }

    public void addOutput(Instance... outputs) {
        Collections.addAll(this.outputs, outputs);
    }

    public Signature getSignature() {
        return signature;
    }

    public void setSignature(Signature signature) {
        this.signature = signature;
    }

    public Attributes getAttributes() {
        return attributes;
    }

    public void setAttributes(Attributes attributes) {
        this.attributes = attributes;
    }

    public AssignManager getManager() {
        return manager;
    }

    public Set<Alias> getAliases() {
        return aliases;
    }

    public boolean isBuiltin() {
        return BuiltinComponent.isBuiltin(identifier);
    }
}
