package pascal.netx.compiler.model;

import pascal.netx.compiler.elaborator.Logger;
import pascal.netx.compiler.exception.CompileCancellationException;
import pascal.netx.compiler.model.expr.Expr;
import pascal.netx.compiler.model.expr.ParallelExpr;
import pascal.netx.compiler.model.expr.VariableRef;
import pascal.netx.compiler.model.variable.*;
import pascal.netx.util.lang.SourceLoc;

import java.util.LinkedList;
import java.util.List;

public final class Entities {

    public static Bundle requiresDataBundle(SourceLoc loc, Entity entity) {
        if (entity instanceof Bundle bundle) {
            return bundle;
        } else {
            Logger.error(loc, "Expected data bundle but got {}", entity);
            throw new CompileCancellationException();
        }
    }

    public static Entity requiresListedValue(SourceLoc loc, Entity entity) {
        if (entity instanceof EntityList(var list)) {
            for (var element : list) {
                requiresListedValue(loc, element);
            }
            return entity;
        } else {
            return requiresValue(loc, entity);
        }
    }

    public static EntityList requiresList(SourceLoc loc, Entity entity) {
        if (entity instanceof EntityList list) {
            return list;
        } else {
            Logger.error(loc, "Expected list but got {}", entity);
            throw new CompileCancellationException();
        }
    }

    public static Value requiresValue(SourceLoc loc, Entity entity) {
        if (entity instanceof Value value) {
            return value;
        } else {
            Logger.error(loc, "Expected value but got {}", entity);
            throw new CompileCancellationException();
        }
    }

    public static Mutable requiresMutable(SourceLoc loc, Entity entity) {
        if (entity instanceof Mutable instance) {
            return instance;
        } else {
            Logger.error(loc, "Expected instance but got {}", entity);
            throw new CompileCancellationException();
        }
    }

    public static Mutable.Factory<?> requiresMutableFactory(SourceLoc loc, Entity entity) {
        if (entity instanceof Mutable.Factory<?> factory) {
            return factory;
        } else {
            Logger.error(loc, "Expected component factory but got {}", entity);
            throw new CompileCancellationException();
        }
    }

    public static IntegerValue requiresIntegerValue(SourceLoc loc, Entity entity) {
        if (entity instanceof IntegerValue integerValue) {
            return integerValue;
        } else {
            Logger.error(loc, "Expected integer rhs but got {}", entity);
            throw new CompileCancellationException();
        }
    }

    public static BooleanValue requiresBooleanValue(SourceLoc loc, Entity entity) {
        if (entity instanceof BooleanValue booleanValue) {
            return booleanValue;
        } else {
            Logger.error(loc, "Expected boolean rhs but got {}", entity);
            throw new CompileCancellationException();
        }
    }

    public static BooleanValue castToBooleanValue(SourceLoc loc, Entity entity) {
        return switch (entity) {
            case BooleanValue booleanValue -> booleanValue;
            case IntegerValue integerValue -> BooleanValue.of(integerValue.toInt() != 0);
            default -> {
                Logger.error(loc, "Cannot cast {} to boolean", entity);
                throw new CompileCancellationException();
            }
        };
    }

    public static Expr castToInstantiateExpr(SourceLoc loc, Entity entity) {
        return switch (entity) {
            case EntityList list -> makeParallelTypeFromList(list, loc);
            case Expr expr -> expr;
            case Variable inst -> VariableRef.of(inst, loc);
            default -> {
                Logger.error(loc, "Cannot cast {} to component type", entity);
                throw new CompileCancellationException();
            }
        };
    }

    private static ParallelExpr makeParallelTypeFromList(EntityList list, SourceLoc sourceLoc) {
        List<Expr> result = new LinkedList<>();
        list.forEach(entity -> result.add(castToInstantiateExpr(sourceLoc, entity)));
        return ParallelExpr.from(result);
    }
}
