package pascal.hardscript.hson.verilog;

import pascal.hardscript.hson.verilog.model.*;
import pascal.hardscript.hson.verilog.model.ref.Constant;
import pascal.hardscript.hson.verilog.model.ref.DataRef;
import pascal.hardscript.hson.verilog.model.ref.DataRefSelect;
import pascal.hardscript.hson.verilog.model.ref.WireRef;
import pascal.hardscript.util.lang.BuiltinComponent;
import pascal.hardscript.util.lang.Identifier;
import pascal.hardscript.util.print.NotepadWriter;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class VerilogDumper {
    private final NotepadWriter notepad;

    private final List<VModule> design;

    public VerilogDumper(NotepadWriter notepad, List<VModule> design) {
        this.notepad = notepad;
        this.design = design;
    }

    public void dump() {
        design.forEach(this::dumpModule);
    }

    private void dumpModule(VModule module) {
        notepad.println("// @(" + module.getId().getSourceLoc() + ")");
        notepad.println("module " + instNameOf(module) + ";");
        notepad.incIndent();
        for (var str : module.getInputs()) {
            notepad.println("input " + str + ";");
        }
        for (var str : module.getOutputs()) {
            notepad.println("output " + str + ";");
        }
        for (var wire : module.getWires()) {
            notepad.print("wire " + typeOf(wire) + idOf(wire.id()) + ";");
            if (wire.id().getSourceLoc() != null) {
                notepad.println("\t\t// @(" + wire.id().getSourceLoc() + ")");
            } else {
                notepad.newLine();
            }
        }
        module.getInstantiations().forEach(this::dumpInstantiation);
        module.getAssigns().forEach(this::dumpAssign);
        dumpRegisters(module.getRegisters());
        notepad.decIndent();
        notepad.println("endmodule");
        notepad.newLine();
    }

    private void dumpInstantiation(Instantiation inst) {
        notepad.print(instNameOf(inst.getModule()));
        notepad.print(" $i_" + inst.getInstIndex() + "(");
        List<DataRef> ports = new LinkedList<>();
        ports.addAll(inst.getInputs());
        ports.addAll(inst.getOutputs());
        var iter = ports.iterator();
        if (iter.hasNext()) {
            notepad.print(dumpDataRef(iter.next()));
        }
        while (iter.hasNext()) {
            notepad.print(", ");
            notepad.print(dumpDataRef(iter.next()));
        }
        notepad.println(");");
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
            default -> {
                var delimiter = " " + BuiltinComponent.VERILOG_OP.get(assign.builtin()) + " ";
                notepad.print(String.join(delimiter, rhsList));
            }
        }
        notepad.println(";");
    }

    private void dumpRegisters(List<Register> registers) {
        Map<DataRef, Map<Boolean, List<Register>>> groups =
                registers.stream().collect(
                        Collectors.groupingBy(Register::clock,
                                Collectors.groupingBy(Register::isPosTrigger)));
        for (var entry : groups.entrySet()) {
            var clock = entry.getKey();
            var regs = entry.getValue();
            if (!regs.get(false).isEmpty()) {
                dumpRegProcedure(regs.get(false), clock, false);
            }
            if (!regs.get(true).isEmpty()) {
                dumpRegProcedure(regs.get(true), clock, true);
            }
        }
    }

    private void dumpRegProcedure(List<Register> regs, DataRef clock, boolean isPosTrigger) {
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
            notepad.print(dumpDataRef(reg.out()));
            notepad.print(" <= ");
            notepad.print(dumpDataRef(reg.in()));
            notepad.println(";");
        }
        notepad.decIndent();
        notepad.println("end");
    }

    private String dumpDataRef(DataRef ref) {
        return switch (ref) {
            case Constant constant -> constant.value().toString();
            case DataRefSelect access -> dumpDataRef(access.base()) + access.selector().toString();
            case WireRef wireRef -> {
                String result = "";
                if (wireRef.instance() != null) {
                    result += "$i_" + wireRef.instance().getInstIndex() + ".";
                }
                result += wireRef.name();
                yield result;
            }
        };
    }

    private static String typeOf(Wire wire) {
        if (wire.width() == 1) {
            return "";
        } else {
            return String.format("[%d:0]", wire.width() - 1);
        }
    }

    private String idOf(Identifier id) {
        return id.name();
    }

    private String instNameOf(VModule module) {
        return module.getId().name() + "_" + module.getDefIndex();
    }
}
