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

    public Identifier getIdentifier() {
        return identifier;
    }

    public List<Component> getInputs() {
        return Collections.unmodifiableList(inputs);
    }

    public List<Component> getOutputs() {
        return Collections.unmodifiableList(outputs);
    }

    public void addInput(Component input) {
        inputs.add(input);
    }

    public void addInput(Component... input) {
        Collections.addAll(inputs, input);
    }

    public void addOutput(Component output) {
        outputs.add(output);
    }

    ModuleScope(Scope parent, Identifier identifier) {
        super(parent);
        this.identifier = identifier;
    }
}
