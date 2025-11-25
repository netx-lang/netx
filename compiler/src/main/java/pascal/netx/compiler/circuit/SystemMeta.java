package pascal.netx.compiler.circuit;

import pascal.netx.compiler.circuit.scope.binding.AttrBindList;
import pascal.netx.compiler.circuit.signal.BooleanValue;
import pascal.netx.compiler.circuit.signal.Int32Value;
import pascal.netx.compiler.circuit.signal.IntegerValue;
import pascal.netx.compiler.utils.CompileCancellationException;
import pascal.netx.compiler.utils.Logger;
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

    private static Entity log2(SourceLoc loc, AttrBindList args) {
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

    private static Entity print(SourceLoc ignored, AttrBindList args) {
        var sb = new StringBuilder();
        for (var arg : args.getArguments()) {
            sb.append(arg.toString());
        }
        Logger.print(sb.toString());
        return EList.ofEmpty();
    }

    private static Entity contains(SourceLoc loc, AttrBindList args) {
        if (args.size() != 2) {
            Logger.error(loc, "SystemMeta contains requires exactly two attributes, found: {}", args.size());
            throw new CompileCancellationException();
        }

        var list = Entities.requiresList(args.getArguments().get(0));
        var element = args.getArguments().get(1);
        return BooleanValue.of(list.contains(element));
    }

    private static Entity empty(SourceLoc loc, AttrBindList args) {
        if (args.size() != 1) {
            Logger.error(loc, "SystemMeta empty requires exactly one argument, found: {}", args.size());
            throw new CompileCancellationException();
        }

        var list = Entities.requiresList(args.getArguments().getFirst());
        return BooleanValue.of(list.isEmpty());
    }

    private static Entity length(SourceLoc loc, AttrBindList args) {
        if (args.size() != 1) {
            Logger.error(loc, "SystemMeta length requires exactly one argument, found: {}", args.size());
            throw new CompileCancellationException();
        }

        var list = Entities.requiresList(args.getArguments().getFirst());
        return Int32Value.of(list.size());
    }

    public Entity apply(SourceLoc loc, AttrBindList parameters) {
        return function.apply(loc, parameters);
    }

    @FunctionalInterface
    interface SystemMetaFunction {
        Entity apply(SourceLoc loc, AttrBindList parameters);
    }
}
