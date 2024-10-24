package pascal.hardscript.compiler.ast.expr.list;

import pascal.hardscript.compiler.ast.Symbol;

public record GeneratorQual(Symbol head, ListExpr list) implements Qualifier {

    public static GeneratorQual of(Symbol head, ListExpr list) {
        return new GeneratorQual(head, list);
    }
}
