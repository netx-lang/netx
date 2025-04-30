package pascal.netx.compiler.analysis.constraint;

public sealed interface DataConstraint permits BinaryConstraint, SelectConstraint, UnaryConstraint {

    boolean holds();

    String error();
}
