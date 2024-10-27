package pascal.hardscript.compiler.elaborator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pascal.hardscript.compiler.ast.SourceLoc;
import pascal.hardscript.compiler.exception.CompileCancellationException;
import pascal.hardscript.compiler.model.Entity;
import pascal.hardscript.compiler.model.ListEntity;
import pascal.hardscript.compiler.model.components.Component;
import pascal.hardscript.compiler.model.connection.instantiation.InstanceType;
import pascal.hardscript.compiler.model.connection.port.Port;
import pascal.hardscript.compiler.model.value.BooleanValue;
import pascal.hardscript.compiler.model.value.IntegerValue;
import pascal.hardscript.compiler.model.value.Value;

import java.util.function.Predicate;

public final class Types {
    private static final Logger log = LoggerFactory.getLogger(Types.class);

    public static InstanceType requiresInstanceType(SourceLoc loc, Entity entity) {
        if (entity instanceof InstanceType instanceType) {
            return instanceType;
        } else {
            log.error("{}: Expected instance type but got {}", loc, entity);
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

    public static ListEntity requiresValueList(SourceLoc loc, Entity entity) {
        var list = requiresList(loc, entity);
        if (list.entities().stream().anyMatch(Predicate.not(Value.class::isInstance))) {
            log.error("{}: Expected list of values but got {}", loc, list);
            throw new CompileCancellationException();
        }
        return list;
    }
}
