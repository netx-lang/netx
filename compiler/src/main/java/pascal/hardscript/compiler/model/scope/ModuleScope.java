package pascal.hardscript.compiler.model.scope;

import pascal.hardscript.compiler.ast.Identifier;
import pascal.hardscript.compiler.model.components.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ModuleScope extends Scope {
    private final Identifier identifier;
    private final List<Component> inputs = new ArrayList<>();
    private final List<Component> outputs = new ArrayList<>();

    public ModuleScope(Scope parent, Identifier identifier) {
        super(parent);
        this.identifier = identifier;
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public List<Component> getInputs() {
        return Collections.unmodifiableList(inputs);
    }

    public List<Component> getOutputs() {
        return Collections.unmodifiableList(outputs);
    }

    public void addInput(Component... inputs) {
        Collections.addAll(this.inputs, inputs);
    }

    public void addOutput(Component... outputs) {
        Collections.addAll(this.outputs, outputs);
    }
}
