package pascal.hardscript.compiler.ast.expr.list;

import pascal.hardscript.compiler.ast.Identifier;

public record GeneratorQual(Identifier head, ListExpr list) implements Qualifier {

    public static GeneratorQual of(Identifier head, ListExpr list) {
        return new GeneratorQual(head, list);
    }
}
