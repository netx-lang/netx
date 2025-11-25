package pascal.netx.compiler.circuit.expr;

public sealed interface InstantiationRef extends Expr permits BuiltinRef, ComponentRef, ImportRef {
}
