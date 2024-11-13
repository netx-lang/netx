package pascal.hardscript.compiler.model.component;

import pascal.hardscript.compiler.model.instance.Data;
import pascal.hardscript.compiler.model.instance.DataAccess;
import pascal.hardscript.compiler.model.instance.Instance;
import pascal.hardscript.compiler.model.instance.Value;
import pascal.hardscript.util.exception.ShallNotReachHereError;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public final class Signature {
    private int nameCount = 0;

    private final ComponentAtom caller;
    private final List<String> inputName = new LinkedList<>();
    private final List<String> outputName = new LinkedList<>();

    public Signature(ComponentAtom caller) {
        this.caller = caller;
    }

    public static Signature of(ComponentAtom caller) {
        return new Signature(caller);
    }

    public void setInputName(List<Instance> inputs) {
        extractNameFromInstances(inputs, inputName);
    }

    public void setOutputName(List<Instance> outputs) {
        extractNameFromInstances(outputs, outputName);
    }

    private void extractNameFromInstances(List<Instance> from, List<String> to) {
        for (var inst : from) {
            var name = switch (inst) {
                case Data data -> data.getIdentifier().name();
                case DataAccess ignored -> allocateIOName();
                case Value ignored -> throw new ShallNotReachHereError();
            };
            to.add(name);
        }
    }

    public ComponentAtom getCaller() {
        return caller;
    }

    public List<String> getInputname() {
        return inputName;
    }

    public List<String> getOutputName() {
        return outputName;
    }

    private String allocateIOName() {
        return "$t" + nameCount++;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Signature signature = (Signature) o;
        return Objects.equals(caller, signature.caller)
                && Objects.equals(inputName, signature.inputName)
                && Objects.equals(outputName, signature.outputName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(caller, inputName, outputName);
    }
}
