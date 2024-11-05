package pascal.hardscript.util.lang;

public enum BuiltinComponent {
    REGISTER, SOURCE, IGNORE, SPLIT, TO,
    EXT, CONCAT,
    OR, NOT, AND, XOR, NOR, NAND,
    EQ, NE, LT, LE, GT, GE,
    ADD, SUB, MUL, DIV, MOD, SHL, ASHR, LSHR;

    public static BuiltinComponent of(String name) {
        return BuiltinComponent.valueOf(name.toUpperCase());
    }
}
