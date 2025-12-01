package pascal.netx.compiler.backend.analysis.model;

import pascal.netx.compiler.io.Logger;
import pascal.netx.compiler.model.circuit.scope.ComponentScope;
import pascal.netx.compiler.model.circuit.scope.PackageScope;
import pascal.netx.compiler.model.circuit.signal.Access;
import pascal.netx.compiler.model.circuit.signal.Clock;
import pascal.netx.compiler.model.circuit.signal.Data;
import pascal.netx.compiler.model.circuit.signal.Wire;
import pascal.netx.compiler.model.circuit.signal.selector.BitSelector;
import pascal.netx.compiler.model.circuit.signal.selector.PartSelector;
import pascal.netx.compiler.model.circuit.signal.selector.Selector;
import pascal.netx.compiler.model.type.BitType;
import pascal.netx.compiler.utils.TypeCheckingException;
import pascal.netx.util.exception.ShallNotReachHereError;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

public final class IOAnalysis extends ModelAnalysis {
    private final Map<Wire, WireCoverage> def = new HashMap<>();
    private final Map<Wire, WireCoverage> use = new HashMap<>();

    public IOAnalysis(PackageScope design) {
        super(design);
    }

    private static void errorDefineInput(Data data) {
        Logger.error("component defines its input {}", data);
        throw new TypeCheckingException();
    }

    private static void warnUnusedInput(Data data) {
        Logger.warn("component has unused bits of input {}", data);
    }

    private static void warnMultipleDriver(Data data) {
        Logger.warn("component has multiple driver to its output {}", data);
    }

    private static void errorUndefOut(Data data) {
        Logger.error("component has undefined bits of output {}", data);
        throw new TypeCheckingException();
    }

    private static boolean needCheck(ComponentScope scope) {
        if (scope.isBuiltin()) {
            return false;
        }
        return !scope.isInterface();
    }

    private static void initializeData(Map<Wire, WireCoverage> coverage, Wire wire) {
        if (coverage.containsKey(wire)) {
            coverage.get(wire).update();
        } else {
            var cover = new WireCoverage(wire);
            cover.update();
            coverage.put(wire, cover);
        }
    }

    private static void initializeDataAccess(Map<Wire, WireCoverage> coverage, Access access) {
        var base = access.base();
        var selector = access.selector();
        if (coverage.containsKey(base)) {
            coverage.get(base).update(selector);
        } else {
            var cover = new WireCoverage(base);
            cover.update(selector);
            coverage.put(base, cover);
        }
    }

    @Override
    public void analyze() {
        pack.forEachSubComponent(scope -> {
            if (needCheck(scope)) {
                analyze(scope);
            }
        });
    }

    private void analyze(ComponentScope component) {
        var manager = component.assigns();
        for (var inst : manager.getDefCount()) {
            initialize(inst, def);
        }
        for (var inst : manager.getUseCount()) {
            initialize(inst, use);
        }

        checkInputIntegrity(component);
        checkOutputIntegrity(component);
    }

    private void checkInputIntegrity(ComponentScope component) {
        for (var inst : component.getInputs()) {
            if (inst instanceof Data data) {
                switch (data) {
                    case Wire wire -> checkInputData(wire);
                    case Access access -> checkInputDataAccess(access);
                    default -> throw new ShallNotReachHereError("Should be eliminated by CompositeDataBlaster");
                }
            }
        }
    }

    private void checkInputData(Wire wire) {
        if (!(wire instanceof Clock)) {
            if (!(use.containsKey(wire) && use.get(wire).isAllSet())) {
                warnUnusedInput(wire);
            } else if (def.containsKey(wire)) {
                errorDefineInput(wire);
            }
        }
    }

    private void checkInputDataAccess(Access access) {
        var base = access.base();
        var selector = access.selector();
        if (!(use.containsKey(base) && use.get(base).isSetFor(selector))) {
            warnUnusedInput(access);
        } else if (def.containsKey(base) && def.get(base).isClearFor(selector)) {
            errorDefineInput(access);
        }
    }

    private void checkOutputIntegrity(ComponentScope component) {
        for (var inst : component.getOutputs()) {
            if (inst instanceof Data data) {
                switch (data) {
                    case Wire wire -> checkOutputData(wire);
                    case Access access -> checkOutputDataAccess(access);
                    default -> throw new ShallNotReachHereError("Should be eliminated by CompositeDataBlaster");
                }
            }
        }
    }

    private void checkOutputData(Wire wire) {
        if (!(def.containsKey(wire) && def.get(wire).isAllSet())) {
            errorUndefOut(wire);
        } else if (def.get(wire).isDoubleSet()) {
            warnMultipleDriver(wire);
        }
    }

    private void checkOutputDataAccess(Access access) {
        var base = access.base();
        var selector = access.selector();
        if (!(def.containsKey(base) && def.get(base).isSetFor(selector))) {
            errorUndefOut(access);
        } else if (def.get(base).isDoubleSet()) {
            warnMultipleDriver(access);
        }
    }

    private void initialize(Data inst, Map<Wire, WireCoverage> coverage) {
        switch (inst) {
            case Wire wire -> initializeData(coverage, wire);
            case Access access -> initializeDataAccess(coverage, access);
            default -> {
                // do nothing
            }
        }
    }

    private static class WireCoverage {
        private final BitSet bitSet;
        private final int size;
        private boolean doubleSet = false;
        private boolean allSet = false;

        private WireCoverage(Wire wire) {
            if (!(wire.getType() instanceof BitType(int width))) {
                throw new ShallNotReachHereError();
            }
            this.size = width;
            this.bitSet = new BitSet(width);
        }

        public boolean isAllSet() {
            if (allSet) {
                return true;
            } else {
                return bitSet.cardinality() == size;
            }
        }

        public boolean isSetFor(Selector selector) {
            return switch (selector) {
                case BitSelector(var index) -> bitSet.get(index);
                case PartSelector part -> {
                    for (int i = part.lsb(); i <= part.msb(); ++i) {
                        if (!bitSet.get(i)) {
                            yield false;
                        }
                    }
                    yield true;
                }
            };
        }

        public boolean isClearFor(Selector selector) {
            return switch (selector) {
                case BitSelector(var index) -> !bitSet.get(index);
                case PartSelector part -> {
                    for (int i = part.lsb(); i <= part.msb(); ++i) {
                        if (bitSet.get(i)) {
                            yield false;
                        }
                    }
                    yield true;
                }
            };
        }

        public boolean isDoubleSet() {
            return doubleSet;
        }

        public void update(Selector selector) {
            if (!allSet) {
                switch (selector) {
                    case BitSelector bit -> bitSet.set(bit.index());
                    case PartSelector part -> {
                        for (int i = part.lsb(); i <= part.msb(); ++i) {
                            if (bitSet.get(i)) {
                                doubleSet = true;
                            } else {
                                bitSet.set(i);
                            }
                        }
                    }
                }
            } else {
                doubleSet = true;
            }
        }

        public void update() {
            if (allSet) {
                doubleSet = true;
            }
            allSet = true;
        }
    }
}
