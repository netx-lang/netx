package pascal.hardscript.compiler.model.value;

import pascal.hardscript.util.lang.BitVector;

public interface IntegerValue extends Value {

    int toInt();

    BitVector toBitVector();

    IntegerValue add(IntegerValue other);

    IntegerValue sub(IntegerValue other);

    IntegerValue neg();

    IntegerValue bitwiseNot();
}
