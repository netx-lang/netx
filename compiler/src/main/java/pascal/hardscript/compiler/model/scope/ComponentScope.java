package pascal.hardscript.compiler.model.scope;

import pascal.hardscript.compiler.model.Entity;
import pascal.hardscript.compiler.model.instance.Instance;
import pascal.hardscript.util.lang.Identifier;
import pascal.hardscript.util.lang.SourceLoc;

import java.util.*;

public final class ComponentScope extends Scope {
    private final int depth;
    private final Identifier identifier;
    private final List<Instance> inputs = new ArrayList<>();
    private final List<Instance> outputs = new ArrayList<>();
    private final Map<String, Entity> attributes = new HashMap<>();

    public ComponentScope(Scope parent, Identifier identifier) {
        super(parent);
        this.identifier = identifier;
        this.depth = (getUpperComponent() != null) ? getUpperComponent().getDepth() + 1 : 0;
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

    public void addAttribute(String name, Entity entity) {
        attributes.put(name, entity);
    }

    public Map<String, Entity> getAttributes() {
        return attributes;
    }
}
