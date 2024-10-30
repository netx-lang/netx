package pascal.hardscript.compiler.elaborator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pascal.hardscript.compiler.ast.SourceLoc;
import pascal.hardscript.compiler.exception.CompileCancellationException;
import pascal.hardscript.compiler.model.Entity;
import pascal.hardscript.compiler.model.ListEntity;
import pascal.hardscript.compiler.model.components.Component;
import pascal.hardscript.compiler.model.components.Register;
import pascal.hardscript.compiler.model.connection.InstanceType;
import pascal.hardscript.compiler.model.connection.Port;
import pascal.hardscript.compiler.model.value.BooleanValue;
import pascal.hardscript.compiler.model.value.IntegerValue;
import pascal.hardscript.compiler.model.value.Value;

import java.util.List;

public final class Types {
    private static final Logger log = LoggerFactory.getLogger(Types.class);

    public static void requiresMatchedSize(SourceLoc sourceLoc, List<?> from, List<?> to) {
        if (from.size() != to.size()) {
            log.error("{}: Instantiation with mismatched component size: {} vs {}", sourceLoc, from.size(), to.size());
            throw new CompileCancellationException();
        }
    }

    public static void requiresMatchedSize(SourceLoc sourceLoc, int expected, List<?> actual) {
        if (expected != actual.size()) {
            log.error("{}: Expected list of size {} but got {}", sourceLoc, expected, actual);
            throw new CompileCancellationException();
        }
    }

    public static InstanceType requiresInstanceType(SourceLoc loc, Entity entity) {
        if (entity instanceof InstanceType instanceType) {
            return instanceType;
        } else {
            log.error("{}: Expected instance type but got {}", loc, entity);
            throw new CompileCancellationException();
        }
    }

    public static Register requiresRegister(SourceLoc loc, Entity entity) {
        if (entity instanceof Register register) {
            return register;
        } else {
            log.error("{}: Expected component factory but got {}", loc, entity);
            throw new CompileCancellationException();
        }
    }

    public static Component.Factory<?> requiresComponentFactory(SourceLoc loc, Entity entity) {
        if (entity instanceof Component.Factory<?> factory) {
            return factory;
        } else {
            log.error("{}: Expected component factory but got {}", loc, entity);
            throw new CompileCancellationException();
        }
    }

    public static Port requiresPort(SourceLoc loc, Entity entity) {
        if (entity instanceof Port port) {
            return port;
        } else {
            log.error("{}: Expected port but got {}", loc, entity);
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

    public static IntegerValue requiresIntegerValue(SourceLoc loc, Entity entity) {
        if (entity instanceof IntegerValue integerValue) {
            return integerValue;
        } else {
            log.error("{}: Expected integer value but got {}", loc, entity);
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

    public static BooleanValue requiresBooleanValue(SourceLoc loc, Entity entity) {
        if (entity instanceof BooleanValue booleanValue) {
            return booleanValue;
        } else {
            log.error("{}: Expected boolean value but got {}", loc, entity);
            throw new CompileCancellationException();
        }
    }

    public static ListEntity requiresList(SourceLoc loc, Entity entity) {
        if (entity instanceof ListEntity list) {
            return list;
        } else {
            log.error("{}: Expected list but got {}", loc, entity);
            throw new CompileCancellationException();
        }
    }

}
