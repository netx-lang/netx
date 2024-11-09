package pascal.hardscript.util.lang;

import java.util.LinkedHashSet;
import java.util.Set;

public enum BuiltinComponent {
    REGISTER, FROM, IGNORE, SPLIT, TO,
    EXT, CONCAT,
    OR, NOT, AND, XOR, NOR, NAND,
    EQ, NE, SLT, SLE, SGT, SGE, ULT, ULE, UGT, UGE,
    ADD, SUB, SHL, ASHR, LSHR;

    public static final Set<BuiltinComponent> NO_DUMP = Set.of(IGNORE, SPLIT, FROM, TO);

    public static final Set<String> VALUES;

    static {
        VALUES = new LinkedHashSet<>();
        for (var value : values()) {
            VALUES.add(value.name());
        }
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
