package pascal.hardscript.hson.verilog;

import pascal.hardscript.hson.verilog.model.Assign;
import pascal.hardscript.hson.verilog.model.Instantiation;
import pascal.hardscript.hson.verilog.model.VModule;
import pascal.hardscript.hson.verilog.model.Wire;
import pascal.hardscript.hson.verilog.model.ref.Constant;
import pascal.hardscript.hson.verilog.model.ref.DataRef;
import pascal.hardscript.hson.verilog.model.ref.DataRefSelect;
import pascal.hardscript.hson.verilog.model.ref.WireRef;
import pascal.hardscript.util.lang.BuiltinComponent;
import pascal.hardscript.util.lang.Identifier;
import pascal.hardscript.util.print.NotepadWriter;

import java.util.LinkedList;
import java.util.List;

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
            notepad.println("\t\t// @(" + wire.id().getSourceLoc() + ")");
        }
        for (var inst : module.getInstantiations()) {
            dumpInstantiation(inst);
        }
        for (var assign : module.getAssigns()) {
            dumpAssign(assign);
        }
        notepad.decIndent();
        notepad.println("endmodule");
        notepad.newLine();
    }

    private void dumpInstantiation(Instantiation inst) {
        notepad.print(instNameOf(inst.getModule()));
        notepad.print(" $i" + inst.getInstIndex() + "(");
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
        var rhs = String.join(
                " " + BuiltinComponent.VERILOG_OP.get(assign.builtin()) + " ",
                assign.inputs().stream().map(this::dumpDataRef).toList());
        notepad.print("assign ");
        notepad.print(dumpDataRef(assign.outputs().getFirst()));
        notepad.print(" = ");
        notepad.print(rhs);
        notepad.println(";");
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
