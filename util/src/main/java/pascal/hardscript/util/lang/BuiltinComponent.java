package pascal.hardscript.util.lang;

public enum BuiltinComponent {
    REGISTER, SOURCE, IGNORE, SPLIT, TO,
    EXT, CONCAT,
    OR, NOT, AND, XOR, NOR, NAND,
    EQ, NE, SLT, SLE, SGT, SGE, ULT, ULE, UGT, UGE,
    ADD, SUB, SHL, ASHR, LSHR;

    public static BuiltinComponent of(String name) {
        return BuiltinComponent.valueOf(name.toUpperCase());
    }
}
