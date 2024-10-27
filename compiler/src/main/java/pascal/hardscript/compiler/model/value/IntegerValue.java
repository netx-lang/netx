package pascal.hardscript.compiler.model.value;

public interface IntegerValue extends Value {

    int toInt();

    IntegerValue negative();

    IntegerValue bitwiseNot();
}
