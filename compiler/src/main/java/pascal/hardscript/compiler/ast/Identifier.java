package pascal.hardscript.compiler.ast;

public record Identifier(String id) implements Symbol {

    public static Identifier of(String id) {
        return new Identifier(id);
    }
}
