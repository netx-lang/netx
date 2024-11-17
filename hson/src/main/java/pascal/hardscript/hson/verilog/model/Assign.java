package pascal.hardscript.hson.verilog.model;

import pascal.hardscript.hson.verilog.model.ref.DataRef;
import pascal.hardscript.util.lang.BuiltinComponent;

import java.util.ArrayList;
import java.util.List;

public record Assign(BuiltinComponent builtin, List<DataRef> inputs, List<DataRef> outputs) {
    public static Assign of(BuiltinComponent builtin, List<DataRef> inputs, List<DataRef> outputs) {
        return new Assign(builtin, new ArrayList<>(inputs), new ArrayList<>(outputs));
    }
}
