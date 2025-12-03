package pascal.netx.compiler.model.circuit;

import pascal.netx.compiler.io.Logger;
import pascal.netx.compiler.model.circuit.expr.Expr;
import pascal.netx.compiler.model.circuit.expr.ParallelExpr;
import pascal.netx.compiler.model.circuit.expr.SignalRef;
import pascal.netx.compiler.model.circuit.signal.*;
import pascal.netx.compiler.utils.CompileCancellationException;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

public final class Entities {

    public static Bundle requireDataBundle(Entity entity) {
        if (entity instanceof Bundle bundle) {
            return bundle;
        } else {
            Logger.error("Expected data bundle but got {}", entity);
            throw new CompileCancellationException();
        }
    }

    public static Entity requireListedValue(Entity entity) {
        if (entity instanceof EList list) {
            for (var element : list) {
                requireListedValue(element);
            }
            return entity;
        } else {
            return requireValue(entity);
        }
    }

    public static EList requireEList(Entity entity) {
        if (entity instanceof EList list) {
            return list;
        } else {
            Logger.error("Expected list but got {}", entity);
            throw new CompileCancellationException();
        }
    }

    public static EList castToEList(Entity entity) {
        if (entity instanceof EList list) {
            return list;
        } else {
            return EList.of(entity);
        }
    }

    public static Value requireValue(Entity entity) {
        if (entity instanceof Value value) {
            return value;
        } else {
            Logger.error("Expected value but got {}", entity);
            throw new CompileCancellationException();
        }
    }

    public static FloatValue castToFloat(int size, Value value) {
        return switch (value) {
            case FloatValue(int _, var _value) -> FloatValue.of(size, _value);
            case VectorValue(var _value) -> FloatValue.of(size, new BigDecimal(_value.asUnsignedBI()));
            default -> {
                Logger.error("Cannot cast {} to float", value);
                throw new CompileCancellationException();
            }
        };
    }

    public static Data requireMutable(Entity entity) {
        if (entity instanceof Data instance) {
            return instance;
        } else {
            Logger.error("Expected instance but got {}", entity);
            throw new CompileCancellationException();
        }
    }

    public static VectorValue requireVectorValue(Entity entity) {
        if (entity instanceof VectorValue value) {
            return value;
        } else {
            Logger.error("Expected integer rhs but got {}", entity);
            throw new CompileCancellationException();
        }
    }

    public static BooleanValue requireBooleanValue(Entity entity) {
        if (entity instanceof BooleanValue booleanValue) {
            return booleanValue;
        } else {
            Logger.error("Expected boolean rhs but got {}", entity);
            throw new CompileCancellationException();
        }
    }

    public static BooleanValue castToBooleanValue(Entity entity) {
        return switch (entity) {
            case BooleanValue booleanValue -> booleanValue;
            case VectorValue integerValue -> BooleanValue.of(!integerValue.isZero());
            default -> {
                Logger.error("Cannot cast {} to boolean", entity);
                throw new CompileCancellationException();
            }
        };
    }

    public static Expr castToCircuitExpr(Entity entity) {
        return switch (entity) {
            case Expr expr -> expr;
            case EList list -> makeParallelTypeFromList(list);
            case Signal inst -> SignalRef.of(inst);
            default -> {
                Logger.error("Cannot cast {} to circuit expression", entity);
                throw new CompileCancellationException();
            }
        };
    }

    private static ParallelExpr makeParallelTypeFromList(EList list) {
        List<Expr> result = new LinkedList<>();
        list.forEach(entity -> result.add(castToCircuitExpr(entity)));
        return ParallelExpr.from(result);
    }
}
