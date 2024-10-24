package pascal.hardscript.compiler.ast.expr.list;

import pascal.hardscript.compiler.ast.expr.atom.SymbolExpr;

public record GeneratorQualifier(SymbolExpr head, ListExpr list) implements Qualifier {

    public static GeneratorQualifier of(SymbolExpr head, ListExpr list) {
        return new GeneratorQualifier(head, list);
    }
}
