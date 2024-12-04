package pascal.hardscript.compiler.model.scope;

import pascal.hardscript.compiler.analysis.AssignManager;
import pascal.hardscript.compiler.model.component.Signature;
import pascal.hardscript.compiler.model.instance.Instance;
import pascal.hardscript.util.lang.BuiltinComponent;
import pascal.hardscript.util.lang.Identifier;
import pascal.hardscript.util.lang.SourceLoc;

import java.util.*;

public final class ComponentScope extends Scope {
    private final boolean isInterface;
    private final Identifier identifier;
    private final Set<Alias> aliases = new HashSet<>();

    private Signature signature;
    private Attributes attributes;
    private final AssignManager manager = new AssignManager(this);
    private List<Instance> inputs = new ArrayList<>();
    private List<Instance> outputs = new ArrayList<>();

    ComponentScope(GlobalScope global, boolean isInterface, Identifier identifier) {
        super(global);
        this.isInterface = isInterface;
        this.identifier = identifier;
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

    public List<Instance> getInputs() {
        return inputs;
    }

    public List<Instance> getOutputs() {
        return outputs;
    }

    public void addInput(Instance... inputs) {
        Collections.addAll(this.inputs, inputs);
    }

    public void addOutput(Instance... outputs) {
        Collections.addAll(this.outputs, outputs);
    }

    public void setInputs(List<Instance> inputs) {
        this.inputs = inputs;
    }

    public void setOutputs(List<Instance> outputs) {
        this.outputs = outputs;
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

    @Override
    public ComponentScope instantiateComponentScope(Identifier identifier) {
        var child = new ComponentScope(this.getGlobal(), false, identifier);
        addChild(child);
        return child;
    }

    @Override
    public ComponentScope instantiateInterfaceScope(Identifier identifier) {
        var child = new ComponentScope(this.getGlobal(), true, identifier);
        addChild(child);
        return child;
    }
}
