package pascal.netx.compiler.backend.analysis.circuit.constraint;

public sealed interface WireConstraint permits BinaryConstraint, ConcatConstraint, UnaryConstraint {

    boolean holds();

    String error();
}
