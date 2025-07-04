package pascal.netx.compiler.analysis.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pascal.netx.compiler.exception.TypeCheckingException;
import pascal.netx.compiler.model.scope.ComponentScope;
import pascal.netx.compiler.model.scope.PackageScope;
import pascal.netx.compiler.model.variable.Access;
import pascal.netx.compiler.model.variable.Clock;
import pascal.netx.compiler.model.variable.Data;
import pascal.netx.compiler.model.variable.Mutable;
import pascal.netx.compiler.model.variable.selector.BitSelector;
import pascal.netx.compiler.model.variable.selector.PartSelector;
import pascal.netx.compiler.model.variable.selector.Selector;
import pascal.netx.compiler.model.variable.type.BitType;
import pascal.netx.util.exception.ShallNotReachHereError;
import pascal.netx.util.lang.SourceLoc;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

public final class IOAnalysis extends ModelAnalysis {
    private static final Logger log = LoggerFactory.getLogger(IOAnalysis.class);
    private final Map<Data, DataCoverage> def = new HashMap<>();
    private final Map<Data, DataCoverage> use = new HashMap<>();

    public IOAnalysis(PackageScope design) {
        super(design);
    }

    private static void errorDefineInput(SourceLoc sourceLoc, Mutable mutable) {
        log.error("{}: component defines its input {}", sourceLoc, mutable);
        throw new TypeCheckingException();
    }

    private static void warnUnusedInput(SourceLoc sourceLoc, Mutable mutable) {
        log.warn("{}: component has unused bits of input {}", sourceLoc, mutable);
    }

    private static void warnMultipleDriver(SourceLoc sourceLoc, Mutable mutable) {
        log.warn("{}: component has multiple driver to its output {}", sourceLoc, mutable);
    }

    private static void errorUndefOut(SourceLoc sourceLoc, Mutable mutable) {
        log.error("{}: component has undefined bits of output {}", sourceLoc, mutable);
        throw new TypeCheckingException();
    }

    private static boolean needCheck(ComponentScope scope) {
        if (scope.isBuiltin()) {
            return false;
        }
        return !scope.isInterface();
    }

    private static void initializeData(Map<Data, DataCoverage> coverage, Data data) {
        if (coverage.containsKey(data)) {
            coverage.get(data).update();
        } else {
            var cover = new DataCoverage(data);
            cover.update();
            coverage.put(data, cover);
        }
    }

    private static void initializeDataAccess(Map<Data, DataCoverage> coverage, Access access) {
        var base = access.base();
        var selector = access.selector();
        if (coverage.containsKey(base)) {
            coverage.get(base).update(selector);
        } else {
            var cover = new DataCoverage(base);
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

        var sourceLoc = component.getSourceLoc();
        checkInputIntegrity(component, sourceLoc);
        checkOutputIntegrity(component, sourceLoc);
    }

    private void checkInputIntegrity(ComponentScope component, SourceLoc sourceLoc) {
        for (var inst : component.getInputs()) {
            if (inst instanceof Mutable mutable) {
                switch (mutable) {
                    case Data data -> checkInputData(sourceLoc, data);
                    case Access access -> checkInputDataAccess(sourceLoc, access);
                    default -> throw new ShallNotReachHereError("Should be eliminated by CompositeDataBlaster");
                }
            }
        }
    }

    private void checkInputData(SourceLoc sourceLoc, Data data) {
        if (!(data instanceof Clock)) {
            if (!(use.containsKey(data) && use.get(data).isAllSet())) {
                warnUnusedInput(sourceLoc, data);
            } else if (def.containsKey(data)) {
                errorDefineInput(sourceLoc, data);
            }
        }
    }

    private void checkInputDataAccess(SourceLoc sourceLoc, Access access) {
        var base = access.base();
        var selector = access.selector();
        if (!(use.containsKey(base) && use.get(base).isSetFor(selector))) {
            warnUnusedInput(sourceLoc, access);
        } else if (def.containsKey(base) && def.get(base).isClearFor(selector)) {
            errorDefineInput(sourceLoc, access);
        }
    }

    private void checkOutputIntegrity(ComponentScope component, SourceLoc sourceLoc) {
        for (var inst : component.getOutputs()) {
            if (inst instanceof Mutable mutable) {
                switch (mutable) {
                    case Data data -> checkOutputData(sourceLoc, data);
                    case Access access -> checkOutputDataAccess(sourceLoc, access);
                    default -> throw new ShallNotReachHereError("Should be eliminated by CompositeDataBlaster");
                }
            }
        }
    }

    private void checkOutputData(SourceLoc sourceLoc, Data data) {
        if (!(def.containsKey(data) && def.get(data).isAllSet())) {
            errorUndefOut(sourceLoc, data);
        } else if (def.get(data).isDoubleSet()) {
            warnMultipleDriver(sourceLoc, data);
        }
    }

    private void checkOutputDataAccess(SourceLoc sourceLoc, Access access) {
        var base = access.base();
        var selector = access.selector();
        if (!(def.containsKey(base) && def.get(base).isSetFor(selector))) {
            errorUndefOut(sourceLoc, access);
        } else if (def.get(base).isDoubleSet()) {
            warnMultipleDriver(sourceLoc, access);
        }
    }

    private void initialize(Mutable inst, Map<Data, DataCoverage> coverage) {
        switch (inst) {
            case Data data -> initializeData(coverage, data);
            case Access access -> initializeDataAccess(coverage, access);
            default -> {
                // do nothing
            }
        }
    }

    private static class DataCoverage {
        private final BitSet bitSet;
        private final int size;
        private boolean doubleSet = false;
        private boolean allSet = false;

        private DataCoverage(Data data) {
            if (!(data.getType() instanceof BitType(int width))) {
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
                case BitSelector(var index) -> bitSet.get(index.toInt());
                case PartSelector(var from, var to) -> {
                    int msb = Math.max(from.toInt(), to.toInt());
                    int lsb = Math.min(from.toInt(), to.toInt());
                    for (int i = lsb; i <= msb; ++i) {
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
                case BitSelector(var index) -> !bitSet.get(index.toInt());
                case PartSelector(var from, var to) -> {
                    int msb = Math.max(from.toInt(), to.toInt());
                    int lsb = Math.min(from.toInt(), to.toInt());
                    for (int i = lsb; i <= msb; ++i) {
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
                    case BitSelector bit -> bitSet.set(bit.index().toInt());
                    case PartSelector part -> {
                        int msb = Math.max(part.start().toInt(), part.end().toInt());
                        int lsb = Math.min(part.start().toInt(), part.end().toInt());
                        for (int i = lsb; i <= msb; ++i) {
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
