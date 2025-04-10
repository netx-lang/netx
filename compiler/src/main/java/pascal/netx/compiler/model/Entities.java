package pascal.netx.compiler.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pascal.netx.compiler.exception.CompileCancellationException;
import pascal.netx.compiler.model.expr.Expr;
import pascal.netx.compiler.model.expr.InstantiationRef;
import pascal.netx.compiler.model.expr.ParallelExpr;
import pascal.netx.compiler.model.expr.VariableRef;
import pascal.netx.compiler.model.variable.*;
import pascal.netx.util.lang.SourceLoc;

import java.util.LinkedList;
import java.util.List;

public final class Entities {
    private static final Logger log = LoggerFactory.getLogger(Entities.class);

    public static Bundle requiresDataBundle(SourceLoc loc, Entity entity) {
        if (entity instanceof Bundle bundle) {
            return bundle;
        } else {
            log.error("{}: Expected data bundle but got {}", loc, entity);
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

    public static Value requiresValue(SourceLoc loc, Entity entity) {
        if (entity instanceof Value value) {
            return value;
        } else {
            log.error("{}: Expected rhs but got {}", loc, entity);
            throw new CompileCancellationException();
        }
    }

    public static InstantiationRef requiresInstantiationRef(SourceLoc loc, Entity entity) {
        if (entity instanceof InstantiationRef instantiationRef) {
            return instantiationRef;
        } else {
            log.error("{}: Expected instantiation ref but got {}", loc, entity);
            throw new CompileCancellationException();
        }
    }

    public static Mutable requiresMutable(SourceLoc loc, Entity entity) {
        if (entity instanceof Mutable instance) {
            return instance;
        } else {
            log.error("{}: Expected instance but got {}", loc, entity);
            throw new CompileCancellationException();
        }
    }

    public static Mutable.Factory<?> requiresMutableFactory(SourceLoc loc, Entity entity) {
        if (entity instanceof Mutable.Factory<?> factory) {
            return factory;
        } else {
            log.error("{}: Expected component factory but got {}", loc, entity);
            throw new CompileCancellationException();
        }
    }

    public static IntegerValue requiresIntegerValue(SourceLoc loc, Entity entity) {
        if (entity instanceof IntegerValue integerValue) {
            return integerValue;
        } else {
            log.error("{}: Expected integer rhs but got {}", loc, entity);
            throw new CompileCancellationException();
        }
    }

    public static BooleanValue requiresBooleanValue(SourceLoc loc, Entity entity) {
        if (entity instanceof BooleanValue booleanValue) {
            return booleanValue;
        } else {
            log.error("{}: Expected boolean rhs but got {}", loc, entity);
            throw new CompileCancellationException();
        }
    }

    public static BooleanValue castToBooleanValue(SourceLoc loc, Entity entity) {
        return switch (entity) {
            case BooleanValue booleanValue -> booleanValue;
            case IntegerValue integerValue -> BooleanValue.of(integerValue.toInt() != 0);
            default -> {
                log.error("{}: Cannot cast {} to boolean", loc, entity);
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
                log.error("{}: Cannot cast {} to component type", loc, entity);
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
