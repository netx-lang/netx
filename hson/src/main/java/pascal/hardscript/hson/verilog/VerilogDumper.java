package pascal.hardscript.hson.verilog;

import pascal.hardscript.hson.verilog.model.Instantiation;
import pascal.hardscript.hson.verilog.model.VModule;
import pascal.hardscript.hson.verilog.model.Wire;
import pascal.hardscript.hson.verilog.model.ref.Constant;
import pascal.hardscript.hson.verilog.model.ref.DataRef;
import pascal.hardscript.hson.verilog.model.ref.DataRefSelect;
import pascal.hardscript.hson.verilog.model.ref.WireRef;
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
            dumpDataRef(iter.next());
        }
        while (iter.hasNext()) {
            notepad.print(", ");
            dumpDataRef(iter.next());
        }
        notepad.println(");");
    }

    private void dumpDataRef(DataRef ref) {
        switch (ref) {
            case Constant constant -> notepad.print(constant.value().toString());
            case DataRefSelect access -> {
                dumpDataRef(access.base());
                notepad.print(access.selector().toString());
            }
            case WireRef wireRef -> {
                if (wireRef.instance() != null) {
                    notepad.print("$i_" + wireRef.instance().getInstIndex() + ".");
                }
                notepad.print(wireRef.name());
            }
        }
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
