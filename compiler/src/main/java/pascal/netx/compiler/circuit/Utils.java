package pascal.netx.compiler.circuit;

import pascal.netx.compiler.NXCompiler;
import pascal.netx.compiler.circuit.scope.ComponentScope;
import pascal.netx.compiler.circuit.scope.PackageScope;
import pascal.netx.compiler.circuit.scope.RepoScope;
import pascal.netx.compiler.circuit.scope.binding.AttrBindList;
import pascal.netx.compiler.circuit.variable.BooleanValue;
import pascal.netx.compiler.circuit.variable.IntegerValue;
import pascal.netx.compiler.utils.CompileCancellationException;
import pascal.netx.compiler.utils.Logger;
import pascal.netx.util.exception.ShallNotReachHereError;

import java.util.List;
import java.util.stream.IntStream;

public final class Utils {
    public static int requireEqSize(List<?> target, List<?> source, String message) {
        if (target.size() != source.size()) {
            Logger.error("{}\nMismatched component size: {} vs {}", message, target.size(), source.size());
            throw new CompileCancellationException();
        }
        return target.size();
    }

    public static void requireEqSize(int expected, List<?> actual, String message) {
        if (expected != actual.size()) {
            Logger.error("{}\nExpected list of size {} but got {}", message, expected, actual.size());
            throw new CompileCancellationException();
        }
    }

    public static void requireEqSize(int expected, AttrBindList actual, String message) {
        if (expected != actual.size()) {
            Logger.error("{}\nExpected list of size {} but got {}", message, expected, actual.size());
            throw new CompileCancellationException();
        }
    }

    public static RepoScope repoOf(PackageScope pack) {
        return pack.getProject();
    }

    public static RepoScope repoOf(ComponentScope component) {
        return repoOf(component.getPackage());
    }

    public static NXCompiler compilerOf(ComponentScope component) {
        return compilerOf(component.getPackage());
    }

    public static NXCompiler compilerOf(PackageScope pack) {
        return pack.getProject().getCompiler();
    }

    public static boolean isEqualListedValue(Entity lhs, Entity rhs) {
        switch (lhs) {
            case EntityList(var lhsList) -> {
                if (rhs instanceof EntityList(var rhsList)) {
                    if (lhsList.size() == rhsList.size()) {
                        return IntStream.range(0, lhsList.size())
                                .allMatch(i -> isEqualListedValue(lhsList.get(i), rhsList.get(i)));
                    }
                }
            }
            case IntegerValue lhsInt -> {
                if (rhs instanceof IntegerValue rhsInt) {
                    return lhsInt.eq(rhsInt).value();
                }
            }
            case BooleanValue lhsBool -> {
                if (rhs instanceof BooleanValue rhsBool) {
                    return lhsBool.eq(rhsBool).value();
                }
            }
            default -> throw new ShallNotReachHereError();
        }
        return false;
    }
}
