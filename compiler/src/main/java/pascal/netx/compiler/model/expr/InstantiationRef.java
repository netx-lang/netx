package pascal.netx.compiler.model.expr;

public sealed interface InstantiationRef extends Expr permits BuiltinRef, ComponentRef, ImportRef {
}
