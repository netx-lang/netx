package pascal.hardscript.compiler.model.scope;

import pascal.hardscript.compiler.model.component.Signature;
import pascal.hardscript.compiler.model.instance.Instance;
import pascal.hardscript.util.lang.BuiltinComponent;
import pascal.hardscript.util.lang.Identifier;
import pascal.hardscript.util.lang.SourceLoc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ComponentScope extends Scope {
    private Signature signature;
    private Attributes attributes;
    private final Identifier identifier;
    private final List<Instance> inputs = new ArrayList<>();
    private final List<Instance> outputs = new ArrayList<>();

    public ComponentScope(Scope parent, Identifier identifier) {
        super(parent);
        this.identifier = identifier;
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

    public boolean isBuiltin() {
        return BuiltinComponent.isBuiltin(identifier);
    }
}
