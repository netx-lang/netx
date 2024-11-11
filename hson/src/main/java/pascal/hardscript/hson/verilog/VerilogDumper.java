package pascal.hardscript.hson.verilog;

import pascal.hardscript.hson.verilog.model.VModule;
import pascal.hardscript.hson.verilog.model.Wire;
import pascal.hardscript.util.lang.Identifier;
import pascal.hardscript.util.print.NotepadWriter;

import java.util.HashMap;
import java.util.Map;

public class VerilogDumper {
    private final NotepadWriter notepad;

    private final VModule design;
    private final Map<Identifier, Integer> idCount = new HashMap<>();
    private final Map<String, Integer> instCount = new HashMap<>();

    public VerilogDumper(NotepadWriter notepad, VModule design) {
        this.notepad = notepad;
        this.design = design;
    }

    public void dump() {
        dumpModule(design.getChildren().getFirst());
    }

    private void dumpModule(VModule module) {
        notepad.println("module " + instNameOf(module.getOrigin()) + "();");
        notepad.incIndent();
        for (var wire : module.getWires()) {
            notepad.println("wire " + typeOf(wire) + idOf(wire.id()) + ";");
        }
        notepad.decIndent();
        notepad.println("endmodule");
    }

    private static String typeOf(Wire wire) {
        if (wire.width() == 1) {
            return "";
        } else {
            return String.format("[%d:0]", wire.width() - 1);
        }
    }

    private String idOf(Identifier id) {
        var count = idCount.getOrDefault(id, 1);
        idCount.put(id, count + 1);
        return String.format("%s_%d_%d_%d", id.name(), id.getSourceLoc().line(), id.getSourceLoc().column(), count);
    }

    private String instNameOf(String name) {
        var count = instCount.getOrDefault(name, 1);
        instCount.put(name, count + 1);
        return name + "_" + count;
    }
}
