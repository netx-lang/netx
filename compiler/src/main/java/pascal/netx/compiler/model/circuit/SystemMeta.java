package pascal.netx.compiler.model.circuit;

import pascal.netx.compiler.io.Logger;
import pascal.netx.compiler.model.circuit.scope.binding.AttrBindList;
import pascal.netx.compiler.model.circuit.signal.BooleanValue;
import pascal.netx.compiler.model.circuit.signal.VectorValue;
import pascal.netx.compiler.utils.CompileCancellationException;
import pascal.netx.util.lang.Identifier;

import java.math.BigInteger;

public enum SystemMeta implements Entity {
    LOG2(SystemMeta::log2),
    PRINT(SystemMeta::print),
    EMPTY(SystemMeta::empty),
    LENGTH(SystemMeta::length),
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

    private static Entity log2(AttrBindList args) {
        if (args.size() != 1) {
            Logger.error("SystemMeta log2 requires exactly one argument, found: {}", args.size());
            throw new CompileCancellationException();
        }

        Entity arg = args.getArguments().getFirst();
        if (!(arg instanceof VectorValue(var value))) {
            Logger.error("SystemMeta log2 requires a numeric argument, found: {}", arg.getClass().getSimpleName());
            throw new CompileCancellationException();
        }

        if (value.isZero()) {
            return VectorValue.fromInt(0);
        }
        return VectorValue.fromInt(value.asUnsignedBI().subtract(BigInteger.ONE).bitLength());
    }

    private static Entity print(AttrBindList args) {
        var sb = new StringBuilder();
        for (var arg : args.getArguments()) {
            sb.append(arg.toString());
        }
        Logger.print(sb.toString());
        return EList.ofEmpty();
    }

    private static Entity contains(AttrBindList args) {
        if (args.size() != 2) {
            Logger.error("SystemMeta contains requires exactly two attributes, found: {}", args.size());
            throw new CompileCancellationException();
        }

        var list = Entities.requireEList(args.getArguments().get(0));
        var element = args.getArguments().get(1);
        return BooleanValue.of(list.contains(element));
    }

    private static Entity empty(AttrBindList args) {
        if (args.size() != 1) {
            Logger.error("SystemMeta empty requires exactly one argument, found: {}", args.size());
            throw new CompileCancellationException();
        }

        var list = Entities.requireEList(args.getArguments().getFirst());
        return BooleanValue.of(list.isEmpty());
    }

    private static Entity length(AttrBindList args) {
        if (args.size() != 1) {
            Logger.error("SystemMeta length requires exactly one argument, found: {}", args.size());
            throw new CompileCancellationException();
        }

        var list = Entities.requireEList(args.getArguments().getFirst());
        return VectorValue.fromInt(list.size());
    }

    public Entity apply(AttrBindList parameters) {
        return function.apply(parameters);
    }

    @FunctionalInterface
    interface SystemMetaFunction {
        Entity apply(AttrBindList parameters);
    }
}
