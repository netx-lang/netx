package pascal.netx.compiler.model.variable;

public sealed interface IntegerValue extends Value permits BitVectorValue, Int32Value {

    int toInt();

    IntegerValue ZERO = Int32Value.of(0);

    IntegerValue add(IntegerValue other);

    IntegerValue sub(IntegerValue other);

    IntegerValue mul(IntegerValue other);

    IntegerValue div(IntegerValue other);

    IntegerValue mod(IntegerValue other);

    IntegerValue pow(IntegerValue other);

    IntegerValue and(IntegerValue other);

    IntegerValue or(IntegerValue other);

    IntegerValue xor(IntegerValue other);

    IntegerValue xnor(IntegerValue other);

    IntegerValue ashr(IntegerValue other);

    IntegerValue lshr(IntegerValue other);

    IntegerValue shl(IntegerValue other);

    BooleanValue eq(IntegerValue other);

    BooleanValue ne(IntegerValue other);

    BooleanValue sgt(IntegerValue other);

    BooleanValue sge(IntegerValue other);

    BooleanValue slt(IntegerValue other);

    BooleanValue sle(IntegerValue other);

    BooleanValue ugt(IntegerValue other);

    BooleanValue uge(IntegerValue other);

    BooleanValue ult(IntegerValue other);

    BooleanValue ule(IntegerValue other);

    IntegerValue neg();

    IntegerValue bitwiseNot();
}
