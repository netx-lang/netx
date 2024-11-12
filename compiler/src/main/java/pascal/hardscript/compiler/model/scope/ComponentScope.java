package pascal.hardscript.compiler.model.scope;

import pascal.hardscript.compiler.model.instance.Instance;
import pascal.hardscript.util.lang.Identifier;
import pascal.hardscript.util.lang.SourceLoc;

import java.util.*;

public final class ComponentScope extends Scope {
    private final int depth;
    private final Identifier identifier;
    private final List<Instance> inputs = new ArrayList<>();
    private final List<Instance> outputs = new ArrayList<>();
    private final Map<Identifier, Instance> attributes = new HashMap<>();

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

    public void addAttribute(Identifier name, Instance entity) {
        attributes.put(name, entity);
    }

    public Map<Identifier, Instance> getAttributes() {
        return attributes;
    }
}
