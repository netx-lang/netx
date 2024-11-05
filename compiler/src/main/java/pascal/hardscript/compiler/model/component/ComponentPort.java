package pascal.hardscript.compiler.model.component;

import pascal.hardscript.compiler.model.Entity;
import pascal.hardscript.compiler.model.instance.Instance;

import java.util.List;

public record ComponentPort(List<Instance> from, List<Instance> to) implements Entity {
    public static ComponentPort of(List<Instance> from, List<Instance> to) {
        return new ComponentPort(from, to);
    }
}
