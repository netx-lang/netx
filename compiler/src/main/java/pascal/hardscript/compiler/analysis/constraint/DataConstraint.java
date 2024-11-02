package pascal.hardscript.compiler.analysis.constraint;

public sealed interface DataConstraint permits BinaryConstraint, UnaryConstraint {

    boolean holds();

    String error();
}
