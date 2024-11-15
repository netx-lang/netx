package pascal.hardscript.compiler.model.component;

import pascal.hardscript.compiler.model.instance.Instance;
import pascal.hardscript.compiler.model.instance.type.InstanceType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static pascal.hardscript.compiler.Configuration.FULLY_EXPAND;

public final class Signature {
    private final ComponentAtom caller;
    private final List<Instance> inputs, outputs;
    private final List<InstanceType> inTypes, outTypes;

    public Signature(ComponentAtom caller, List<Instance> inputs, List<Instance> outputs) {
        this.caller = caller;
        this.inputs = inputs;
        this.outputs = outputs;
        inTypes = inputs.stream().map(Instance::getType).toList();
        outTypes = outputs.stream().map(Instance::getType).toList();
    }

    public static Signature of(ComponentAtom caller, List<Instance> inputName, List<Instance> outputName) {
        return new Signature(caller, new ArrayList<>(inputName), new ArrayList<>(outputName));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Signature signature = (Signature) o;
        boolean calleeEq = Objects.equals(caller.getDefinition(), signature.caller.getDefinition());
        if (calleeEq) {
            if (FULLY_EXPAND) {
                return Objects.equals(inputs, signature.inputs) && Objects.equals(outputs, signature.outputs);
            } else {
                return Objects.equals(inTypes, signature.inTypes) && Objects.equals(outTypes, signature.outTypes);
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (FULLY_EXPAND) {
            return Objects.hash(caller.getDefinition(), inputs, outputs);
        } else {
            return Objects.hash(caller.getDefinition(), inTypes, outTypes);
        }
    }

    public ComponentAtom caller() {
        return caller;
    }

    public List<Instance> inputs() {
        return inputs;
    }

    public List<Instance> outputs() {
        return outputs;
    }
}

