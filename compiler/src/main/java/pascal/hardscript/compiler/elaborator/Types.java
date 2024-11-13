package pascal.hardscript.compiler.elaborator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pascal.hardscript.compiler.exception.CompileCancellationException;
import pascal.hardscript.compiler.model.Entity;
import pascal.hardscript.compiler.model.EntityList;
import pascal.hardscript.compiler.model.component.BuiltinAtom;
import pascal.hardscript.compiler.model.component.ComponentType;
import pascal.hardscript.compiler.model.component.ParallelType;
import pascal.hardscript.compiler.model.instance.BooleanValue;
import pascal.hardscript.compiler.model.instance.Data;
import pascal.hardscript.compiler.model.instance.Instance;
import pascal.hardscript.compiler.model.instance.IntegerValue;
import pascal.hardscript.util.lang.BuiltinComponent;
import pascal.hardscript.util.lang.SourceLoc;

import java.util.LinkedList;
import java.util.List;

public final class Types {
    private static final Logger log = LoggerFactory.getLogger(Types.class);

    public static int requiresMatchedSize(SourceLoc sourceLoc, List<?> target, List<?> source) {
        if (target.size() != source.size()) {
            log.error("{}: Instantiation with mismatched component size: {} vs {}",
                    sourceLoc, target.size(), source.size());
            throw new CompileCancellationException();
        }
        return target.size();
    }

    public static void requiresMatchedSize(SourceLoc sourceLoc, int expected, List<?> actual) {
        if (expected != actual.size()) {
            log.error("{}: Expected list of size {} but got {}", sourceLoc, expected, actual);
            throw new CompileCancellationException();
        }
    }

    public static Instance requiresInstance(SourceLoc loc, Entity entity) {
        if (entity instanceof Instance instance) {
            return instance;
        } else {
            log.error("{}: Expected component but got {}", loc, entity);
            throw new CompileCancellationException();
        }
    }

    public static Data.Factory<?> requiresDataFactory(SourceLoc loc, Entity entity) {
        if (entity instanceof Data.Factory<?> factory) {
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

    public static ComponentType castToComponentType(SourceLoc loc, Entity entity) {
        return switch (entity) {
            case EntityList list -> makeParallelTypeFromList(loc, list);
            case Instance inst -> BuiltinAtom.of(BuiltinComponent.FROM, loc, List.of(inst));
            case ComponentType componentType -> componentType;
            default -> {
                log.error("{}: Cannot cast {} to component type", loc, entity);
                throw new CompileCancellationException();
            }
        };
    }

    private static ParallelType makeParallelTypeFromList(SourceLoc sourceLoc, EntityList list) {
        List<ComponentType> result = new LinkedList<>();
        list.forEach(entity -> result.add(castToComponentType(sourceLoc, entity)));
        return ParallelType.from(result);
    }
}
