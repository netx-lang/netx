package pascal.hardscript.hson.verilog;

import pascal.hardscript.hson.verilog.model.*;
import pascal.hardscript.hson.verilog.model.ref.Constant;
import pascal.hardscript.hson.verilog.model.ref.DataRef;
import pascal.hardscript.hson.verilog.model.ref.WireRef;
import pascal.hardscript.hson.verilog.model.ref.WireRefSelect;
import pascal.hardscript.util.lang.BuiltinComponent;
import pascal.hardscript.util.lang.Identifier;
import pascal.hardscript.util.print.NotepadWriter;

import java.util.Collection;
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
        new VerilogAnalysis(design).analyze();
    }

    private static String typeOf(Wire wire) {
        if (wire.width() == 1) {
            return "";
        } else {
            return String.format("[%d:0] ", wire.width() - 1);
        }
    }

    public void dump() {
        design.forEach(this::dumpModule);
        notepad.flush();
        notepad.close();
    }

    private void dumpModule(VModule module) {
        notepad.println("// @(" + module.getId().getSourceLoc() + ")");
        notepad.println("module " + instNameOf(module) + "(");
        notepad.incIndent();

        List<String> ports = new LinkedList<>();
        ports.addAll(module.getInputs());
        ports.addAll(module.getOutputs());
        var iter = ports.iterator();
        if (iter.hasNext()) {
            notepad.print(iter.next());
            iter.forEachRemaining(str -> notepad.print(", " + str));
        }
        notepad.newLine();
        notepad.decIndent();
        notepad.println(");");
        notepad.incIndent();
        for (var str : module.getInputs()) {
            notepad.println("input " + str + ";");
        }
        for (var str : module.getOutputs()) {
            notepad.println("output " + str + ";");
        }
        for (var wire : module.getAllWires()) {
            notepad.print("wire " + typeOf(wire) + idOf(wire.id()) + ";");
            if (wire.id().getSourceLoc() != null) {
                notepad.println(" // @(" + wire.id().getSourceLoc() + ")");
            } else {
                notepad.newLine();
            }
        }
        for (var reg : module.getAllRegisters()) {
            var wire = reg.getTrueReg();
            notepad.print("reg " + typeOf(wire) + idOf(wire.id()) + ";");
            if (wire.id().getSourceLoc() != null) {
                notepad.println(" // @(" + wire.id().getSourceLoc() + ")");
            } else {
                notepad.newLine();
            }
        }
        module.getInstantiations().forEach(this::dumpInstantiation);
        module.getAssigns().forEach(this::dumpAssign);
        dumpRegisters(module.getAllRegisters());
        notepad.decIndent();
        notepad.println("endmodule");
        notepad.newLine();
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
            default -> {
                var delimiter = " " + BuiltinComponent.VERILOG_OP.get(assign.builtin()) + " ";
                notepad.print(String.join(delimiter, rhsList));
            }
        }
        notepad.println(";");
    }

    private void dumpInstantiation(Instantiation inst) {
        notepad.print(instNameOf(inst.getModule()));
        notepad.print(" inst_" + inst.getInstIndex() + "(");
        List<DataRef> port = new LinkedList<>();
        port.addAll(inst.getInputs());
        port.addAll(inst.getOutputs());
        var iter = port.iterator();
        if (iter.hasNext()) {
            notepad.print(dumpDataRef(iter.next()));
            iter.forEachRemaining(str -> notepad.print(", " + dumpDataRef(str)));
        }
        notepad.println(");");
    }

    private void dumpRegProcedure(List<Register> regs, DataRef clock, boolean isPosTrigger) {
        for (var reg : regs) {
            notepad.println("assign " + dumpDataRef(reg.out()) + " = " + idOf(reg.getTrueReg().id()) + ";");
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
            notepad.print(idOf(reg.getTrueReg().id()));
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
        var str =  switch (ref) {
            case Constant constant -> constant.value().toString();
            case WireRefSelect access -> dumpDataRef(access.base()) + access.selector().toString();
            case WireRef wireRef -> {
                String result = "";
                if (wireRef.instance() != null) {
                    result += "inst_" + wireRef.instance().getInstIndex() + ".";
                }
                result += wireRef.name();
                yield result;
            }
        };
        return str.replace("$", "");
    }

    private String idOf(Identifier id) {
        return id.name().replace("$", "");
    }

    private String instNameOf(VModule module) {
        return module.getId().name().replace("$", "") + "_" + module.getDefIndex();
    }
}
