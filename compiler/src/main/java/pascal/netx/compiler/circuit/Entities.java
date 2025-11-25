package pascal.netx.compiler.circuit;

import pascal.netx.compiler.circuit.expr.Expr;
import pascal.netx.compiler.circuit.expr.ParallelExpr;
import pascal.netx.compiler.circuit.expr.VariableRef;
import pascal.netx.compiler.circuit.variable.*;
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
        if (entity instanceof EntityList(var list)) {
            for (var element : list) {
                requiresListedValue(element);
            }
            return entity;
        } else {
            return requiresValue(entity);
        }
    }

    public static EntityList requiresList(Entity entity) {
        if (entity instanceof EntityList list) {
            return list;
        } else {
            Logger.error("Expected list but got {}", entity);
            throw new CompileCancellationException();
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

    public static Mutable requiresMutable(Entity entity) {
        if (entity instanceof Mutable instance) {
            return instance;
        } else {
            Logger.error("Expected instance but got {}", entity);
            throw new CompileCancellationException();
        }
    }

    public static Mutable.Factory<?> requiresMutableFactory(Entity entity) {
        if (entity instanceof Mutable.Factory<?> factory) {
            return factory;
        } else {
            Logger.error("Expected component factory but got {}", entity);
            throw new CompileCancellationException();
        }
    }

    public static IntegerValue requiresIntegerValue(Entity entity) {
        if (entity instanceof IntegerValue integerValue) {
            return integerValue;
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
            case IntegerValue integerValue -> BooleanValue.of(integerValue.toInt() != 0);
            default -> {
                Logger.error("Cannot cast {} to boolean", entity);
                throw new CompileCancellationException();
            }
        };
    }

    public static Expr castToInstantiateExpr(Entity entity) {
        return switch (entity) {
            case EntityList list -> makeParallelTypeFromList(list);
            case Expr expr -> expr;
            case Variable inst -> VariableRef.of(inst);
            default -> {
                Logger.error("Cannot cast {} to component type", entity);
                throw new CompileCancellationException();
            }
        };
    }

    private static ParallelExpr makeParallelTypeFromList(EntityList list) {
        List<Expr> result = new LinkedList<>();
        list.forEach(entity -> result.add(castToInstantiateExpr(entity)));
        return ParallelExpr.from(result);
    }
}
