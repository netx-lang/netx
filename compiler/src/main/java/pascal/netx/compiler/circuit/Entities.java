package pascal.netx.compiler.circuit;

import pascal.netx.compiler.circuit.expr.Expr;
import pascal.netx.compiler.circuit.expr.ParallelExpr;
import pascal.netx.compiler.circuit.expr.SignalRef;
import pascal.netx.compiler.circuit.signal.*;
import pascal.netx.compiler.utils.CompileCancellationException;
import pascal.netx.compiler.utils.Logger;

import java.util.LinkedList;
import java.util.List;

public final class Entities {

    public static Bundle requiresDataBundle(Entity entity) {
        if (entity instanceof Bundle bundle) {
            return bundle;
        } else {
            Logger.error("Expected data bundle but got {}", entity);
            throw new CompileCancellationException();
        }
    }

    public static Entity requiresListedValue(Entity entity) {
        if (entity instanceof EList list) {
            for (var element : list) {
                requiresListedValue(element);
            }
            return entity;
        } else {
            return requiresValue(entity);
        }
    }

    public static EList requiresEList(Entity entity) {
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

    public static Value requiresValue(Entity entity) {
        if (entity instanceof Value value) {
            return value;
        } else {
            Logger.error("Expected value but got {}", entity);
            throw new CompileCancellationException();
        }
    }

    public static Data requiresMutable(Entity entity) {
        if (entity instanceof Data instance) {
            return instance;
        } else {
            Logger.error("Expected instance but got {}", entity);
            throw new CompileCancellationException();
        }
    }

    public static Data.Factory<?> requiresMutableFactory(Entity entity) {
        if (entity instanceof Data.Factory<?> factory) {
            return factory;
        } else {
            Logger.error("Expected component factory but got {}", entity);
            throw new CompileCancellationException();
        }
    }

    public static VectorValue requiresVectorValue(Entity entity) {
        if (entity instanceof VectorValue value) {
            return value;
        } else {
            Logger.error("Expected integer rhs but got {}", entity);
            throw new CompileCancellationException();
        }
    }

    public static BooleanValue requiresBooleanValue(Entity entity) {
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
