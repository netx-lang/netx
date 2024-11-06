package pascal.hardscript.util.lang;

import java.util.Set;

public enum BuiltinComponent {
    REGISTER, FROM, IGNORE, SPLIT, TO,
    EXT, CONCAT,
    OR, NOT, AND, XOR, NOR, NAND,
    EQ, NE, SLT, SLE, SGT, SGE, ULT, ULE, UGT, UGE,
    ADD, SUB, SHL, ASHR, LSHR;

    public static final Set<BuiltinComponent> NO_DUMP = Set.of(IGNORE, SPLIT, FROM, TO);

    public static BuiltinComponent of(String name) {
        return BuiltinComponent.valueOf(name.toUpperCase());
    }
}
