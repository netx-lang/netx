package pascal.hardscript.verilog;

import pascal.hardscript.util.exception.ShallNotReachHereError;
import pascal.hardscript.util.lang.BuiltinComponent;
import pascal.hardscript.util.lang.Identifier;
import pascal.hardscript.verilog.model.Assign;
import pascal.hardscript.verilog.model.Instantiation;
import pascal.hardscript.verilog.model.VModule;
import pascal.hardscript.verilog.model.Wire;
import pascal.hardscript.verilog.model.ref.Constant;
import pascal.hardscript.verilog.model.ref.DataRef;
import pascal.hardscript.verilog.model.ref.WireRef;
import pascal.hardscript.verilog.model.ref.WireRefSelect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class VerilogAnalysis {
    private final List<VModule> design;

    public VerilogAnalysis(List<VModule> design) {
        this.design = design;
    }

    public void analyze() {
        for (var module : design) {
            new ModuleAnalysis(module).analyze();
        }
    }

    private static class ModuleAnalysis {
        private final Supplier<Integer> tempIndexer = new AtomicInteger(0)::getAndIncrement;
        private final Map<DataRef, WireRef> tempWires = new HashMap<>();
        private final VModule module;
        private int constantPortCount = 0;

        private ModuleAnalysis(VModule module) {
            this.module = module;
        }

        public void analyze() {
            analyzeConstantPorts();
            analyzeInstRef();
            analyzeRegister();
        }

        public void analyzeConstantPorts() {
            analyzeConstantInputs();
            analyzeConstantOutputs();
        }

        private void analyzeConstantInputs() {
            if (module.getInputs().stream().anyMatch(Constant.class::isInstance)) {
                List<DataRef> newInputs = new ArrayList<>();
                for (var ref : module.getInputs()) {
                    if (ref instanceof Constant(var value)) {
                        var wire = Wire.of(Identifier.of("$c_" + constantPortCount++), value.size());
                        module.add(wire);
                        newInputs.add(WireRef.of(null, wire.id().name()));
                    } else {
                        newInputs.add(ref);
                    }
                }
                module.setInputs(newInputs);
            }
        }

        private void analyzeConstantOutputs() {
            if (module.getOutputs().stream().anyMatch(Constant.class::isInstance)) {
                List<DataRef> newOutputs = new ArrayList<>();
                for (var ref : module.getOutputs()) {
                    if (ref instanceof Constant(var value)) {
                        var wire = Wire.of(Identifier.of("$c_" + constantPortCount++), value.size());
                        var wireRef = WireRef.of(null, wire.id().name());
                        newOutputs.add(wireRef);
                        module.add(wire);
                        var assign = Assign.of(BuiltinComponent.TO);
                        assign.setInputs(List.of(ref));
                        assign.setOutputs(List.of(wireRef));
                        module.add(-1, assign);
                    } else {
                        newOutputs.add(ref);
                    }
                }
                module.setOutputs(newOutputs);
            }
        }

        public void analyzeInstRef() {
            for (var inst : module.getInstantiations()) {
                inst.getInputs().forEach(ref -> makeTempWireForSelfRef(inst, ref));
                inst.getOutputs().forEach(ref -> makeTempWireForSelfRef(inst, ref));
            }

            for (var inst : module.getInstantiations()) {
                inst.getInputs().replaceAll(this::replaceWithTempWire);
                inst.getOutputs().replaceAll(this::replaceWithTempWire);
            }
            for (var assign : module.getAssigns()) {
                assign.inputs().replaceAll(this::replaceWithTempWire);
                assign.outputs().replaceAll(this::replaceWithTempWire);
            }
            module.getAllRegisters().forEach(
                    reg -> reg.resetRef(this::replaceWithTempWire));
        }

        public void analyzeRegister() {
            for (var reg : module.getAllRegisters()) {
                reg.setTrueReg(makeTempWireForReg(reg.out()));
            }
        }

        private DataRef replaceWithTempWire(DataRef dataRef) {
            return switch (dataRef) {
                case WireRef ref -> tempWires.getOrDefault(ref, ref);
                case WireRefSelect ref -> {
                    var base = replaceWithTempWire(ref.base());
                    yield WireRefSelect.of((WireRef) base, ref.selector());
                }
                default -> dataRef;
            };
        }

        private void makeTempWireForSelfRef(Instantiation inst, DataRef dataRef) {
            switch (dataRef) {
                case WireRef ref -> {
                    if (ref.instance() == inst) {
                        makeTempWireFor(ref);
                    }
                }
                case WireRefSelect ref -> {
                    if (ref.base().instance() == inst) {
                        makeTempWireFor(ref.base());
                    }
                }
                default -> {
                    // do nothing
                }
            }
        }

        private void makeTempWireFor(WireRef ref) {
            int width = getWidthOfWireRef(ref);
            var wire = makeTempWire(width);
            module.add(wire);
            tempWires.put(ref, WireRef.of(null, wire.id().name()));
        }

        private int getWidthOfWireRef(WireRef ref) {
            var parent = ref.instance() == null ? module : ref.instance().getModule();
            return parent.getWire(ref.name()).width();
        }

        private Wire makeTempWireForReg(DataRef dataRef) {
            return switch (dataRef) {
                case WireRef ref -> makeTempWire(getWidthOfWireRef(ref));
                case WireRefSelect ref -> makeTempWire(ref.selector().getWidth());
                default -> throw new ShallNotReachHereError();
            };
        }

        private Wire makeTempWire(int width) {
            var tempName = "$p_" + tempIndexer.get();
            return Wire.of(Identifier.of(tempName), width);
        }

    }
}
