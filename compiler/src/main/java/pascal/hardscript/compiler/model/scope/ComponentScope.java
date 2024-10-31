package pascal.hardscript.compiler.model.scope;

import pascal.hardscript.compiler.ast.Identifier;
import pascal.hardscript.compiler.model.data.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ComponentScope extends Scope {
    private final int depth;
    private final Identifier identifier;
    private final List<Data> inputs = new ArrayList<>();
    private final List<Data> outputs = new ArrayList<>();

    public ComponentScope(Scope parent, Identifier identifier) {
        super(parent);
        this.identifier = identifier;
        this.depth = (getUpperComponent() != null) ? getUpperComponent().getDepth() + 1 : 0;
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
}
