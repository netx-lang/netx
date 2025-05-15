package pascal.netx.compiler.analysis.model.constraint;

public sealed interface DataConstraint permits BinaryConstraint, ConcatConstraint, UnaryConstraint {

    boolean holds();

    String error();
}
