package pascal.hardscript.util.lang;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public enum BuiltinComponent {
    REGISTER, IGNORE, SPLIT, TO,
    EXT, CONCAT,
    OR, NOT, AND, XOR, NOR, NAND, XNOR,
    EQ, NE, SLT, SLE, SGT, SGE, ULT, ULE, UGT, UGE,
    ADD, SUB, SHL, ASHR, LSHR;

    public static final Set<BuiltinComponent> NO_DUMP = Set.of(IGNORE, SPLIT, TO);
    public static final Map<BuiltinComponent, String> VERILOG_OP;
    public static final Set<String> VALUES;

    static {
        VALUES = new LinkedHashSet<>();
        for (var value : values()) {
            VALUES.add(value.name());
        }

        VERILOG_OP = new LinkedHashMap<>();
        VERILOG_OP.put(NOT, "~");
        VERILOG_OP.put(OR, "|");    VERILOG_OP.put(AND, "&");   VERILOG_OP.put(XOR, "^");
        VERILOG_OP.put(NOR, "~|");  VERILOG_OP.put(NAND, "~&"); VERILOG_OP.put(XNOR, "~^");
        VERILOG_OP.put(EQ, "==");   VERILOG_OP.put(NE, "!=");
        VERILOG_OP.put(SLT, "<");   VERILOG_OP.put(ULT, "<");
        VERILOG_OP.put(SLE, "<=");  VERILOG_OP.put(ULE, "<=");
        VERILOG_OP.put(SGT, ">");   VERILOG_OP.put(UGT, ">");
        VERILOG_OP.put(SGE, ">=");  VERILOG_OP.put(UGE, ">=");
        VERILOG_OP.put(ADD, "+");   VERILOG_OP.put(SUB, "-");
        VERILOG_OP.put(SHL, "<<");  VERILOG_OP.put(LSHR, ">>"); VERILOG_OP.put(ASHR, ">>>");
    }

    public static BuiltinComponent of(String name) {
        return BuiltinComponent.valueOf(name.toUpperCase());
    }


    public static BuiltinComponent of(Identifier id) {
        return of(id.name());
    }

    public static boolean isBuiltin(String name) {
        return VALUES.contains(name.toUpperCase());
    }

    public static boolean isBuiltin(Identifier id) {
        return isBuiltin(id.name());
    }
}
