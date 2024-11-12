package pascal.hardscript.compiler.model.scope;

import pascal.hardscript.compiler.model.instance.Instance;
import pascal.hardscript.util.lang.Identifier;
import pascal.hardscript.util.lang.SourceLoc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ComponentScope extends Scope {
    private final int depth;

    private final Identifier identifier;
    private final List<Instance> inputs = new ArrayList<>();
    private final List<Instance> outputs = new ArrayList<>();
    private Attributes attributes;

    public ComponentScope(Scope parent, Identifier identifier) {
        super(parent);
        this.identifier = identifier;
        if (parent instanceof DesignScope) {
            this.depth = 0;
        } else {
            this.depth = parent.getLeastUpperComponent().depth + 1;
        }
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

    public Attributes getAttributes() {
        return attributes;
    }

    public void setAttributes(Attributes attributes) {
        this.attributes = attributes;
        for (var entry : attributes.entrySet()) {
            bind(entry.getKey(), entry.getValue());
        }
    }
}
