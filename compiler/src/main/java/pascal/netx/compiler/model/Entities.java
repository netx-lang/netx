package pascal.netx.compiler.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pascal.netx.compiler.exception.CompileCancellationException;
import pascal.netx.compiler.model.component.Expr;
import pascal.netx.compiler.model.component.InstanceAtom;
import pascal.netx.compiler.model.component.ParallelExpr;
import pascal.netx.compiler.model.instance.*;
import pascal.netx.util.lang.SourceLoc;

import java.util.LinkedList;
import java.util.List;

public final class Entities {
    private static final Logger log = LoggerFactory.getLogger(Entities.class);

    public static DataBundle requiresDataBundle(SourceLoc loc, Entity entity) {
        if (entity instanceof DataBundle bundle) {
            return bundle;
        } else {
            log.error("{}: Expected data bundle but got {}", loc, entity);
            throw new CompileCancellationException();
        }
    }

    public static Value requiresValue(SourceLoc loc, Entity entity) {
        if (entity instanceof Value value) {
            return value;
        } else {
            log.error("{}: Expected value but got {}", loc, entity);
            throw new CompileCancellationException();
        }
    }

    public static MutableInstance requiresMutableInstance(SourceLoc loc, Entity entity) {
        if (entity instanceof MutableInstance instance) {
            return instance;
        } else {
            log.error("{}: Expected instance but got {}", loc, entity);
            throw new CompileCancellationException();
        }
    }

    public static MutableInstance.Factory<?> requiresInstanceFactory(SourceLoc loc, Entity entity) {
        if (entity instanceof MutableInstance.Factory<?> factory) {
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
            log.error("{}: Expected integer value but got {}", loc, entity);
            throw new CompileCancellationException();
        }
    }

    public static BooleanValue requiresBooleanValue(SourceLoc loc, Entity entity) {
        if (entity instanceof BooleanValue booleanValue) {
            return booleanValue;
        } else {
            log.error("{}: Expected boolean value but got {}", loc, entity);
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

    public static EntityList requiresList(SourceLoc loc, Entity entity) {
        if (entity instanceof EntityList list) {
            return list;
        } else {
            log.error("{}: Expected list but got {}", loc, entity);
            throw new CompileCancellationException();
        }
    }

    public static Expr requiresComponentType(SourceLoc loc, Entity entity) {
        if (entity instanceof Expr expr) {
            return expr;
        } else {
            log.error("{}: Expected component type but got {}", loc, entity);
            throw new CompileCancellationException();
        }
    }

    public static Expr castToComponentType(Entity entity, SourceLoc loc) {
        return switch (entity) {
            case EntityList list -> makeParallelTypeFromList(list, loc);
            case Expr expr -> expr;
            case Instance inst -> InstanceAtom.of(InstanceAtom.Mode.BIDI, inst, loc);
            default -> {
                log.error("{}: Cannot cast {} to component type", loc, entity);
                throw new CompileCancellationException();
            }
        };
    }

    private static ParallelExpr makeParallelTypeFromList(EntityList list, SourceLoc sourceLoc) {
        List<Expr> result = new LinkedList<>();
        list.forEach(entity -> result.add(castToComponentType(entity, sourceLoc)));
        return ParallelExpr.from(result);
    }
}
