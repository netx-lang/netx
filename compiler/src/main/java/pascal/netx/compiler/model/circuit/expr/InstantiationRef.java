package pascal.netx.compiler.model.circuit.expr;

public sealed interface InstantiationRef extends Expr permits BuiltinRef, ComponentRef, ImportRef {
}
