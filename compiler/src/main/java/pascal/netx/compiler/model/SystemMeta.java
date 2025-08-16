package pascal.netx.compiler.model;

import pascal.netx.compiler.elaborator.Logger;
import pascal.netx.compiler.exception.CompileCancellationException;
import pascal.netx.compiler.model.scope.binding.ArgBindList;
import pascal.netx.compiler.model.variable.BooleanValue;
import pascal.netx.compiler.model.variable.Int32Value;
import pascal.netx.compiler.model.variable.IntegerValue;
import pascal.netx.util.lang.Identifier;
import pascal.netx.util.lang.SourceLoc;

import java.math.BigInteger;

public enum SystemMeta implements Entity {
    LOG2(SystemMeta::log2),
    PRINT(SystemMeta::print),
    EMPTY(SystemMeta::empty),
    LENGTH(SystemMeta::length), // Alias for empty
    CONTAINS(SystemMeta::contains);

    private final SystemMetaFunction function;

    SystemMeta(SystemMetaFunction function) {
        this.function = function;
    }

    public static SystemMeta resolve(Identifier id) {
        try {
            return SystemMeta.valueOf(id.asSystemMeta());
        } catch (IllegalArgumentException e) {
            Logger.error(id.getSourceLoc(), "Unknown system meta function: {}", id.name());
            throw new CompileCancellationException();
        }
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
        var sb = new StringBuilder();
        for (var arg : args.getArguments()) {
            sb.append(arg.toString());
        }
        Logger.print(sb.toString());
        return EntityList.ofEmpty();
    }

    private static Entity contains(SourceLoc loc, ArgBindList args) {
        if (args.size() != 2) {
            Logger.error(loc, "SystemMeta contains requires exactly two arguments, found: {}", args.size());
            throw new CompileCancellationException();
        }

        var list = Entities.requiresList(loc, args.getArguments().get(0));
        var element = args.getArguments().get(1);
        return BooleanValue.of(list.entities().contains(element));
    }

    private static Entity empty(SourceLoc loc, ArgBindList args) {
        if (args.size() != 1) {
            Logger.error(loc, "SystemMeta empty requires exactly one argument, found: {}", args.size());
            throw new CompileCancellationException();
        }

        var list = Entities.requiresList(loc, args.getArguments().getFirst());
        return BooleanValue.of(list.entities().isEmpty());
    }

    private static Entity length(SourceLoc loc, ArgBindList args) {
        if (args.size() != 1) {
            Logger.error(loc, "SystemMeta length requires exactly one argument, found: {}", args.size());
            throw new CompileCancellationException();
        }

        var list = Entities.requiresList(loc, args.getArguments().getFirst());
        return Int32Value.of(list.entities().size());
    }

    public Entity apply(SourceLoc loc, ArgBindList parameters) {
        return function.apply(loc, parameters);
    }

    @FunctionalInterface
    interface SystemMetaFunction {
        Entity apply(SourceLoc loc, ArgBindList parameters);
    }
}
