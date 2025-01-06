package pascal.netx.compiler.model;

import pascal.netx.compiler.model.expr.ComponentAtom;
import pascal.netx.compiler.model.instance.Instance;
import pascal.netx.compiler.model.instance.type.InstanceType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class Signature {
    private final ComponentAtom caller;
    private List<Instance> inputs, outputs;
    private List<InstanceType> inTypeCache, outTypeCache;

    public Signature(ComponentAtom caller) {
        this.caller = caller;
    }

    public static Signature of(ComponentAtom caller, List<Instance> inputs, List<Instance> outputs) {
        var result = new Signature(caller);
        result.setInputs(inputs);
        result.setOutputs(outputs);
        return result;
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

    public void setInputs(List<Instance> inputs) {
        this.inputs = new ArrayList<>(inputs);
    }

    public void setOutputs(List<Instance> outputs) {
        this.outputs = new ArrayList<>(outputs);
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

