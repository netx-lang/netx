package pascal.netx.compiler.model;

import pascal.netx.compiler.model.expr.ComponentRef;
import pascal.netx.compiler.model.variable.Variable;
import pascal.netx.compiler.model.variable.type.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class Signature {
    private final ComponentRef caller;
    private List<Variable> inputs, outputs;
    private List<Type> inTypeCache, outTypeCache;

    private Signature(ComponentRef caller) {
        this.caller = caller;
    }

    public static Signature of(ComponentRef caller, List<Variable> inputs, List<Variable> outputs) {
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
                && Objects.equals(caller.getArgList(), signature.caller.getArgList())
                && Objects.equals(getInType(), signature.getInType())
                && Objects.equals(getOutType(), signature.getOutType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(caller.getDefinition(), caller.getArgList(), getInType(), getOutType());
    }

    public ComponentRef caller() {
        return caller;
    }

    public List<Variable> inputs() {
        return inputs;
    }

    public List<Variable> outputs() {
        return outputs;
    }

    public void setInputs(List<Variable> inputs) {
        this.inputs = new ArrayList<>(inputs);
    }

    public void setOutputs(List<Variable> outputs) {
        this.outputs = new ArrayList<>(outputs);
    }

    private List<Type> getInType() {
        if (inTypeCache == null) {
            inTypeCache = inputs.stream().map(Variable::getType).toList();
        }
        return inTypeCache;
    }

    private List<Type> getOutType() {
        if (outTypeCache == null) {
            outTypeCache = outputs.stream().map(Variable::getType).toList();
        }
        return outTypeCache;
    }
}

