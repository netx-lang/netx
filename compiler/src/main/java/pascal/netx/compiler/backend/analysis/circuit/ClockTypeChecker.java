package pascal.netx.compiler.backend.analysis.circuit;

import pascal.netx.compiler.backend.analysis.circuit.constraint.WireConstraint;
import pascal.netx.compiler.io.Logger;
import pascal.netx.compiler.model.circuit.Utils;
import pascal.netx.compiler.model.circuit.scope.PackageScope;
import pascal.netx.compiler.model.circuit.signal.*;
import pascal.netx.compiler.utils.TypeCheckingException;
import pascal.netx.util.collection.UnionFind;
import pascal.netx.util.lang.BuiltinComponent;

import java.util.HashSet;
import java.util.List;

/** Checks clock/data separation before width inference erases the distinction. */
public final class ClockTypeChecker extends CircuitAnalysis {
    public ClockTypeChecker(PackageScope pack) {
        super(pack);
    }

    private static List<? extends Signal> flatten(Signal signal) {
        return signal instanceof Composition composition ? composition.flatten() : List.of(signal);
    }

    private static boolean canCarryClock(Signal signal) {
        if (signal instanceof Clock) {
            return true;
        }
        // Inferred user wires and wiring-only primitives may forward a clock.
        if (signal instanceof Wire wire && wire.hasImplicitType()) {
            var parent = wire.getParent();
            return !parent.isBuiltin() || parent.asBuiltin() == BuiltinComponent.TO
                    || parent.asBuiltin() == BuiltinComponent.DUP;
        }
        return false;
    }

    private static void reject(Signal signal) {
        Logger.error("Implicit clock/data conversion is not allowed; use AS_CLK or AS_BIT explicitly:\n{}",
                WireConstraint.describeSignalBlock("signal", signal));
        throw new TypeCheckingException();
    }

    @Override
    public void analyze() {
        var aliases = new UnionFind<Signal>();
        pack.forEachSubComponent(scope -> {
            for (var port : scope.getInputs()) {
                flatten(port).forEach(signal -> aliases.union(signal, signal));
            }
            for (var port : scope.getOutputs()) {
                flatten(port).forEach(signal -> aliases.union(signal, signal));
            }
            for (var alias : scope.getAliases()) {
                var lhs = flatten(alias.lhs());
                var rhs = flatten(alias.rhs());
                int size = Utils.requireEqSize(lhs, rhs, "Clock connection shape mismatch");
                for (int i = 0; i < size; i++) {
                    aliases.union(lhs.get(i), rhs.get(i));
                }
            }
        });
        var clocks = new HashSet<Signal>();
        for (var signal : aliases.elements()) {
            if (signal instanceof Clock) {
                clocks.add(aliases.find(signal));
            }
        }
        for (var signal : aliases.elements()) {
            if (signal instanceof Access(var base, _)
                    && (base instanceof Clock || aliases.contains(base) && clocks.contains(aliases.find(base)))) {
                reject(signal);
            }
            if (clocks.contains(aliases.find(signal)) && !canCarryClock(signal)) {
                reject(signal);
            }
        }
    }
}
