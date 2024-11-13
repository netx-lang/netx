package pascal.hardscript.compiler.model.scope;

import pascal.hardscript.compiler.model.component.AtomType;
import pascal.hardscript.compiler.model.instance.Instance;
import pascal.hardscript.util.lang.Identifier;
import pascal.hardscript.util.lang.SourceLoc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ComponentScope extends Scope {
    private final int depth;

    private AtomType signature;
    private Attributes attributes;
    private final Identifier identifier;
    private final List<Instance> inputs = new ArrayList<>();
    private final List<Instance> outputs = new ArrayList<>();

    public ComponentScope(Scope parent, Identifier identifier) {
        super(parent);
        this.identifier = identifier;
        this.depth = parent instanceof DesignScope ? 0 : parent.getLeastUpperComponent().depth + 1;
    }

    public SourceLoc getSourceLoc() {
        return identifier.getSourceLoc();
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public int getDepth() {
        return depth;
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

    public AtomType getSignature() {
        return signature;
    }

    public void setSignature(AtomType signature) {
        this.signature = signature;
    }

    public Attributes getAttributes() {
        return attributes;
    }

    public void setAttributes(Attributes attributes) {
        this.attributes = attributes;
    }
}
