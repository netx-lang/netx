package pascal.hardscript.compiler.analysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pascal.hardscript.compiler.exception.TypeCheckingException;
import pascal.hardscript.compiler.model.instance.Clock;
import pascal.hardscript.compiler.model.instance.Data;
import pascal.hardscript.compiler.model.instance.DataAccess;
import pascal.hardscript.compiler.model.instance.MutableInstance;
import pascal.hardscript.compiler.model.instance.selector.BitSelector;
import pascal.hardscript.compiler.model.instance.selector.PartSelector;
import pascal.hardscript.compiler.model.instance.selector.Selector;
import pascal.hardscript.compiler.model.instance.type.BitType;
import pascal.hardscript.compiler.model.scope.ComponentScope;
import pascal.hardscript.compiler.model.scope.GlobalScope;
import pascal.hardscript.util.exception.ShallNotReachHereError;
import pascal.hardscript.util.lang.SourceLoc;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

public final class IOAnalysis extends ModelAnalysis{
    private static final Logger log = LoggerFactory.getLogger(IOAnalysis.class);
    private final Map<Data, DataCoverage> def = new HashMap<>();
    private final Map<Data, DataCoverage> use = new HashMap<>();

    public IOAnalysis(GlobalScope design) {
        super(design);
    }

    private static void errorDefineInput(SourceLoc sourceLoc, MutableInstance mutable) {
        log.error("{}: component defines its input {}", sourceLoc, mutable);
        throw new TypeCheckingException();
    }

    private static void warnUnusedInput(SourceLoc sourceLoc, MutableInstance mutable) {
        log.warn("{}: component has unused bits of input {}", sourceLoc, mutable);
    }

    private static void warnMultipleDriver(SourceLoc sourceLoc, MutableInstance mutable) {
        log.warn("{}: component has multiple driver to its output {}", sourceLoc, mutable);
    }

    private static void errorUndefOut(SourceLoc sourceLoc, MutableInstance mutable) {
        log.error("{}: component has undefined bits of output {}", sourceLoc, mutable);
        throw new TypeCheckingException();
    }

    private static boolean needCheck(ComponentScope scope) {
        if (scope.isBuiltin()) {
            return false;
        }
        return !scope.isInterface();
    }

    @Override
    public void analyze() {
        design.forEachSubScope(scope -> {
            if (needCheck(scope)) {
                analyze(scope);
            }
        });
    }

    private void analyze(ComponentScope component) {
        var manager = component.getManager();
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

    private static void initializeData(Map<Data, DataCoverage> coverage, Data data) {
        if (coverage.containsKey(data)) {
            coverage.get(data).update();
        } else {
            var cover = new DataCoverage(data);
            cover.update();
            coverage.put(data, cover);
        }
    }

    private static void initializeDataAccess(Map<Data, DataCoverage> coverage, DataAccess access) {
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

    private void checkInputIntegrity(ComponentScope component, SourceLoc sourceLoc) {
        for (var inst : component.getInputs()) {
            if (inst instanceof MutableInstance mutable) {
                switch (mutable) {
                    case Data data -> checkInputData(sourceLoc, data);
                    case DataAccess access-> checkInputDataAccess(sourceLoc, access);
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

    private void checkInputDataAccess(SourceLoc sourceLoc, DataAccess access) {
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
            if (inst instanceof MutableInstance mutable) {
                switch (mutable) {
                    case Data data -> checkOutputData(sourceLoc, data);
                    case DataAccess access -> checkOutputDataAccess(sourceLoc, access);
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

    private void checkOutputDataAccess(SourceLoc sourceLoc, DataAccess access) {
        var base = access.base();
        var selector = access.selector();
        if (!(def.containsKey(base) && def.get(base).isSetFor(selector))) {
            errorUndefOut(sourceLoc, access);
        } else if (def.get(base).isDoubleSet()) {
            warnMultipleDriver(sourceLoc, access);
        }
    }

    private void initialize(MutableInstance inst, Map<Data, DataCoverage> coverage) {
        switch (inst) {
            case Data data -> initializeData(coverage, data);
            case DataAccess access -> initializeDataAccess(coverage, access);
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
