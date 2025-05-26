package pascal.netx.verilog;

import pascal.netx.util.lang.BuiltinComponent;
import pascal.netx.util.lang.SourceLoc;
import pascal.netx.util.print.NotepadWriter;
import pascal.netx.verilog.model.*;
import pascal.netx.verilog.model.ref.Constant;
import pascal.netx.verilog.model.ref.DataRef;
import pascal.netx.verilog.model.ref.WireRef;
import pascal.netx.verilog.model.ref.WireRefSelect;

import java.util.*;
import java.util.stream.Collectors;

public class VerilogDumper {
    private final NotepadWriter notepad;
    private final Map<VModule, Integer> design = new LinkedHashMap<>();

    public VerilogDumper(NotepadWriter notepad, List<VModule> design) {
        this.notepad = notepad;
        int count = 0;
        for (var module : design) {
            this.design.put(module, count++);
        }
        new VerilogAnalysis(design).analyze();
    }

    private static String typeOf(Wire wire) {
        if (wire.width() == 1) {
            return "";
        } else {
            return String.format("[%d:0] ", wire.width() - 1);
        }
    }

    private static String baseStringOf(DataRef ref) {
        return switch (ref) {
            case WireRefSelect select -> baseStringOf(select.base());
            case WireRef wire -> wire.name();
            case Constant _ -> null;
        };
    }

    public void dump() {
        for (var entry : design.entrySet()) {
            new ModuleDumper().dump(entry.getKey(), entry.getValue());
        }
        notepad.flush();
        notepad.close();
    }

    private static String getHeading(boolean isInput, boolean isOutput) {
        String heading;
        if (isInput && isOutput) {
            heading = "inout";
        } else if (isInput) {
            heading = "input";
        } else if (isOutput) {
            heading = "output";
        } else {
            heading = "wire";
        }
        return heading;
    }

    private class ModuleDumper {
        private final Map<Wire, Integer> regCount = new HashMap<>();
        private final Map<String, Integer> wireCount = new HashMap<>();
        private final Map<Instantiation, Integer> instCount = new HashMap<>();

        private <T> int allocateIn(Map<T, Integer> map, T key) {
            return map.computeIfAbsent(key, _ -> map.size());
        }

        private void dump(VModule module, int index) {
            notepad.println(String.format("// @(%s) %s", module.getId().getSourceLoc(), module.getId() + module.getAttribute()));
            notepad.println(String.format("module mod_%d (", index));
            notepad.incIndent();
            registerWires(module);
            dumpIOPorts(module.getInputs(), module.getOutputs());
            notepad.newLine();
            notepad.decIndent();
            notepad.println(");");
            notepad.incIndent();
            dumpModuleWires(module);
            dumpModuleRegisters(module);
            module.getInstantiations().forEach(this::dumpInstantiation);
            module.getAssigns().forEach(this::dumpAssign);
            dumpRegisters(module.getAllRegisters());
            notepad.decIndent();
            notepad.println("endmodule");
            notepad.newLine();
        }

        private void registerWires(VModule module) {
            module.getAllWires().forEach(wire -> allocateIn(wireCount, wire.id().name()));
        }

        private void dumpModuleRegisters(VModule module) {
            for (var reg : module.getAllRegisters()) {
                var wire = reg.getTrueReg();
                notepad.println(String.format("reg %s r_%s; // %s", typeOf(wire), allocateIn(regCount, wire), wire.id()));
            }
        }

        private void dumpModuleWires(VModule module) {
            var declarations = groupDeclarationsByHeading(module);
            for (var entry : declarations.entrySet()) {
                var wires = entry.getValue().stream().collect(Collectors.groupingBy(WireDeclaration::type));
                for (var wireEntry : wires.entrySet()) {
                    List<WireDeclaration> located = new LinkedList<>(), nonLocated = new LinkedList<>();
                    for (var wire : wireEntry.getValue()) {
                        if (!wire.loc.isNone()) {
                            located.add(wire);
                        } else {
                            nonLocated.add(wire);
                        }
                    }
                    Runnable printHeading = () -> {
                        notepad.print(entry.getKey() + " ");
                        notepad.print(wireEntry.getKey());
                    };
                    dumpLocatedDeclarationByLine(printHeading, located);
                    if (!nonLocated.isEmpty()) {
                        printHeading.run();
                        dumpWireDecl(nonLocated);
                    }
                }
            }
        }

        private void dumpLocatedDeclarationByLine(Runnable printHeading, List<WireDeclaration> declarations) {
            if (declarations.isEmpty()) {
                return;
            }
            for (int i = 0; i < declarations.size(); i += 8) {
                var subList = declarations.subList(i, Math.min(i + 8, declarations.size()));
                dumpWireDeclLocation(subList);
                printHeading.run();
                dumpWireDecl(subList);
                notepad.newLine();
            }
        }

        private Map<String, List<WireDeclaration>> groupDeclarationsByHeading(VModule module) {
            var inputSet = module.getInputs().stream()
                    .map(VerilogDumper::baseStringOf).filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            var outputSet = module.getOutputs().stream()
                    .map(VerilogDumper::baseStringOf).filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            Map<String, List<WireDeclaration>> declarations = new HashMap<>();
            for (var wire : module.getAllWires()) {
                boolean isInput = inputSet.contains(wire.id().name());
                boolean isOutput = outputSet.contains(wire.id().name());
                String heading = getHeading(isInput, isOutput);
                var declaration = new WireDeclaration(typeOf(wire), wire, wire.id().getSourceLoc());
                if (declarations.containsKey(heading)) {
                    declarations.get(heading).add(declaration);
                } else {
                    declarations.put(heading, new LinkedList<>(List.of(declaration)));
                }
            }
            return declarations;
        }

        private void dumpWireDeclLocation(List<WireDeclaration> located) {
            boolean isFirst = true;
            notepad.print("// ");
            for (var decl : located) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    notepad.print(", ");
                }
                notepad.print(decl.wire.id().toStringWithLoc());
            }
            notepad.newLine();
        }

        private void dumpWireDecl(List<WireDeclaration> nonLocated) {
            boolean isFirst = true;
            for (var decl : nonLocated) {
                var id = String.format("t_%d", allocateIn(wireCount, decl.wire.id().name()));
                if (isFirst) {
                    isFirst = false;
                } else {
                    notepad.print(", ");
                }
                notepad.print(id);
            }
            notepad.println(";");
        }

        private void dumpIOPorts(List<DataRef> inputs, List<DataRef> outputs) {
            List<DataRef> ports = new LinkedList<>();
            ports.addAll(inputs);
            ports.addAll(outputs);
            var iter = ports.iterator();
            if (iter.hasNext()) {
                notepad.print(dumpDataRef(iter.next()));
                iter.forEachRemaining(str -> notepad.print(", " + dumpDataRef(str)));
            }
        }

        private void dumpAssign(Assign assign) {
            notepad.print("assign ");
            notepad.print(dumpDataRef(assign.outputs().getFirst()));
            notepad.print(" = ");
            var rhsList = assign.inputs().stream().map(this::dumpDataRef).toList();
            switch (assign.builtin()) {
                case NOT -> {
                    notepad.print("~");
                    notepad.print(rhsList.getFirst());
                }
                case CONCAT -> {
                    notepad.print("{");
                    notepad.print(String.join(", ", rhsList));
                    notepad.print("}");
                }
                case CAST -> {
                    notepad.print("$signed(");
                    notepad.print(rhsList.getFirst());
                    notepad.print(")");
                }
                case TO -> notepad.print(rhsList.getFirst());
                default -> {
                    var delimiter = " " + BuiltinComponent.VERILOG_OP.get(assign.builtin()) + " ";
                    notepad.print(String.join(delimiter, rhsList));
                }
            }
            notepad.println(";");
        }

        private void dumpInstantiation(Instantiation inst) {
            notepad.print(String.format("mod_%d inst_%d (", design.get(inst.getModule()), allocateIn(instCount, inst)));
            dumpIOPorts(inst.getInputs(), inst.getOutputs());
            notepad.println(");");
        }

        private void dumpRegProcedure(List<Register> regs, DataRef clock, boolean isPosTrigger) {
            for (var reg : regs) {
                notepad.println(String.format("assign %s = r_%s;", dumpDataRef(reg.out()), regCount.get(reg.getTrueReg())));
            }
            notepad.print("always @(");
            if (isPosTrigger) {
                notepad.print("posedge ");
            } else {
                notepad.print("negedge ");
            }
            notepad.print(dumpDataRef(clock));
            notepad.println(") begin");
            notepad.incIndent();
            for (var reg : regs) {
                notepad.print("r_" + regCount.get(reg.getTrueReg()));
                notepad.print(" <= ");
                notepad.print(dumpDataRef(reg.in()));
                notepad.println(";");
            }
            notepad.decIndent();
            notepad.println("end");
        }

        private void dumpRegisters(Collection<Register> registers) {
            Map<DataRef, Map<Boolean, List<Register>>> groups =
                    registers.stream().collect(
                            Collectors.groupingBy(Register::clock,
                                    Collectors.groupingBy(Register::isPosTrigger)));
            for (var entry : groups.entrySet()) {
                var clock = entry.getKey();
                var regs = entry.getValue();
                if (regs.get(false) != null) {
                    dumpRegProcedure(regs.get(false), clock, false);
                }
                if (regs.get(true) != null) {
                    dumpRegProcedure(regs.get(true), clock, true);
                }
            }
        }

        private String dumpDataRef(DataRef ref) {
            return switch (ref) {
                case Constant constant -> constant.value().toString();
                case WireRefSelect access -> dumpDataRef(access.base()) + access.selector().toString();
                case WireRef wireRef -> {
                    String result = "";
                    if (wireRef.instance() != null) {
                        result += "inst_" + instCount.get(wireRef.instance()) + ".";
                    }
                    result += "t_" + wireCount.get(wireRef.name());
                    yield result;
                }
            };
        }

        record WireDeclaration(String type, Wire wire, SourceLoc loc) {
        }
    }
}
