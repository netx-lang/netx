package pascal.hardscript.compiler.ast.module;

import pascal.hardscript.compiler.ast.expr.list.ListExpr;

public record CompositeInstRef(ListExpr refList) implements InstRef {

    public static CompositeInstRef of(ListExpr refList) {
        return new CompositeInstRef(refList);
    }
}
