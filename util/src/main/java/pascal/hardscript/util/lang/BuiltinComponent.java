package pascal.hardscript.util.lang;

public enum BuiltinComponent {
    REGISTER, SOURCE, IGNORE, SPLIT, TO, OR, NOT, AND, XOR, NOR, NAND;

    public static BuiltinComponent of(String name) {
        return BuiltinComponent.valueOf(name.toUpperCase());
    }
}
