package pascal.hardscript.compiler.model.component;

import pascal.hardscript.compiler.model.instance.Instance;
import pascal.hardscript.compiler.model.instance.type.InstanceType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class Signature {
    private final ComponentAtom caller;
    private final List<Instance> inputs, outputs;
    private List<InstanceType> inTypeCache, outTypeCache;

    public Signature(ComponentAtom caller, List<Instance> inputs, List<Instance> outputs) {
        this.caller = caller;
        this.inputs = inputs;
        this.outputs = outputs;
    }

    public static Signature of(ComponentAtom caller, List<Instance> inputName, List<Instance> outputName) {
        return new Signature(caller, new ArrayList<>(inputName), new ArrayList<>(outputName));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Signature signature = (Signature) o;
        return Objects.equals(caller.getDefinition(), signature.caller.getDefinition())
                && Objects.equals(caller.getParameters(), signature.caller.getParameters())
                && Objects.equals(getInType(), signature.getInType())
                && Objects.equals(getOutType(), signature.getOutType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(caller.getDefinition(), caller.getParameters(), getInType(), getOutType());
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

    private List<InstanceType> getInType() {
        if (inTypeCache == null) {
            inTypeCache = inputs.stream().map(Instance::getType).toList();
        }
        return inTypeCache;
    }

    private List<InstanceType> getOutType() {
        if (outTypeCache == null) {
            outTypeCache = outputs.stream().map(Instance::getType).toList();
        }
        return outTypeCache;
    }
}

