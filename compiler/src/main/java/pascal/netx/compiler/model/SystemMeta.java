package pascal.netx.compiler.model;

import pascal.netx.compiler.elaborator.Logger;
import pascal.netx.compiler.exception.CompileCancellationException;
import pascal.netx.compiler.model.scope.binding.ArgBindList;
import pascal.netx.compiler.model.variable.Int32Value;
import pascal.netx.compiler.model.variable.IntegerValue;
import pascal.netx.util.lang.Identifier;
import pascal.netx.util.lang.SourceLoc;

import java.math.BigInteger;

public enum SystemMeta implements Entity {
    LOG2(SystemMeta::log2),
    PRINT(SystemMeta::print);

    private final SystemMetaFunction function;

    SystemMeta(SystemMetaFunction function) {
        this.function = function;
    }

    public static SystemMeta resolve(Identifier id) {
        return SystemMeta.valueOf(id.name().toUpperCase());
    }

    public static boolean contains(Identifier id) {
        try {
            SystemMeta.valueOf(id.name().toUpperCase());
        } catch (IllegalArgumentException e) {
            return false;
        }
        return true;
    }

    private static Entity log2(SourceLoc loc, ArgBindList args) {
        if (args.size() != 1) {
            Logger.error(loc, "SystemMeta log2 requires exactly one argument, found: {}", args.size());
            throw new CompileCancellationException();
        }

        Entity arg = args.getArguments().getFirst();
        if (!(arg instanceof IntegerValue value)) {
            Logger.error(loc, "SystemMeta log2 requires a numeric argument, found: {}", arg.getClass().getSimpleName());
            throw new CompileCancellationException();
        }

        var integer = value.toBitVector().toBigInteger();
        if (integer.compareTo(BigInteger.ZERO) == 0) {
            return Int32Value.of(0);
        }
        return Int32Value.of(integer.subtract(BigInteger.ONE).bitLength());
    }

    private static Entity print(SourceLoc ignored, ArgBindList args) {
        var str = args.getArguments().toString();
        Logger.print(str.substring(1, str.length() - 1));
        return EntityList.ofEmpty();
    }

    public Entity apply(SourceLoc loc, ArgBindList parameters) {
        return function.apply(loc, parameters);
    }

    @FunctionalInterface
    interface SystemMetaFunction {
        Entity apply(SourceLoc loc, ArgBindList parameters);
    }
}
