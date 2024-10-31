package pascal.hardscript.compiler.model.scope;

import pascal.hardscript.compiler.ast.Identifier;
import pascal.hardscript.compiler.ast.SourceLoc;
import pascal.hardscript.compiler.model.Entity;
import pascal.hardscript.compiler.model.data.Data;

import java.util.*;

public final class ComponentScope extends Scope {
    private final int depth;
    private final Identifier identifier;
    private final List<Data> inputs = new ArrayList<>();
    private final List<Data> outputs = new ArrayList<>();
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

    public List<Data> getInputs() {
        return Collections.unmodifiableList(inputs);
    }

    public List<Data> getOutputs() {
        return Collections.unmodifiableList(outputs);
    }

    public void addInput(Data... inputs) {
        Collections.addAll(this.inputs, inputs);
    }

    public void addOutput(Data... outputs) {
        Collections.addAll(this.outputs, outputs);
    }

    public void addAttribute(String name, Entity entity) {
        attributes.put(name, entity);
    }

    public Map<String, Entity> getAttributes() {
        return attributes;
    }
}
