package pascal.netx.compiler.analysis.model.constraint;

public sealed interface WireConstraint permits BinaryConstraint, ConcatConstraint, UnaryConstraint {

    boolean holds();

    String error();
}
