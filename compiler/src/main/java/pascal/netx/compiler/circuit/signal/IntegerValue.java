package pascal.netx.compiler.circuit.signal;

import pascal.netx.compiler.ast.expr.BinaryExpr;
import pascal.netx.util.exception.ShallNotReachHereError;

import java.util.function.BiFunction;

public sealed interface IntegerValue extends Value permits BitVectorValue, Int32Value {

    IntegerValue ZERO = Int32Value.of(0);

    static BiFunction<IntegerValue, IntegerValue, Value> dispatchBinaryOp(BinaryExpr.Operator op) {
        return switch (op) {
            case ADD -> IntegerValue::add;
            case SUB -> IntegerValue::sub;
            case MUL -> IntegerValue::mul;
            case DIV -> IntegerValue::div;
            case MOD -> IntegerValue::mod;
            case POW -> IntegerValue::pow;
            case AND -> IntegerValue::and;
            case OR -> IntegerValue::or;
            case XOR -> IntegerValue::xor;
            case XNOR -> IntegerValue::xnor;
            case ARITH_SHR -> IntegerValue::ashr;
            case LOGIC_SHR -> IntegerValue::lshr;
            case ARITH_SHL, LOGIC_SHL -> IntegerValue::shl;
            case EQ -> IntegerValue::eq;
            case NE -> IntegerValue::ne;
            case SLT -> IntegerValue::slt;
            case SLE -> IntegerValue::sle;
            case SGT -> IntegerValue::sgt;
            case SGE -> IntegerValue::sge;
            case ULT -> IntegerValue::ult;
            case ULE -> IntegerValue::ule;
            case UGT -> IntegerValue::ugt;
            case UGE -> IntegerValue::uge;
            default -> throw new ShallNotReachHereError();
        };
    }

    int toInt();

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
