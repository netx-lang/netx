package pascal.hardscript.compiler.model.component;

import pascal.hardscript.compiler.model.instance.Instance;

import java.util.List;
import java.util.Objects;

public record Signature(ComponentAtom caller, List<Instance> inputs, List<Instance> outputs) {

    public static Signature of(ComponentAtom caller, List<Instance> inputName, List<Instance> outputName) {
        return new Signature(caller, inputName, outputName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Signature signature = (Signature) o;
        return Objects.equals(caller, signature.caller);
    }

    @Override
    public int hashCode() {
        return Objects.hash(caller);
    }
}
