package pascal.hardscript.hson.verilog.model;

import pascal.hardscript.util.lang.BuiltinComponent;

import java.util.List;

public record Assign(BuiltinComponent op, List<Data> lhs, List<Data> rhs) {
    public static Assign of(BuiltinComponent op, List<Data> lhs, List<Data> rhs) {
        return new Assign(op, lhs, rhs);
    }
}
