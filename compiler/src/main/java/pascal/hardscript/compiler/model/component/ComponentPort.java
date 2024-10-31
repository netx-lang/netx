package pascal.hardscript.compiler.model.component;

import pascal.hardscript.compiler.model.Entity;
import pascal.hardscript.compiler.model.data.Data;

import java.util.List;

public record ComponentPort(List<Data> from, List<Data> to) implements Entity {
    public static ComponentPort of(List<Data> from, List<Data> to) {
        return new ComponentPort(from, to);
    }
}
