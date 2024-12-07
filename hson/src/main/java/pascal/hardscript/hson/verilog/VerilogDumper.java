package pascal.hardscript.hson.verilog;

import pascal.hardscript.hson.verilog.model.*;
import pascal.hardscript.hson.verilog.model.ref.Constant;
import pascal.hardscript.hson.verilog.model.ref.DataRef;
import pascal.hardscript.hson.verilog.model.ref.WireRef;
import pascal.hardscript.hson.verilog.model.ref.WireRefSelect;
import pascal.hardscript.util.lang.BuiltinComponent;
import pascal.hardscript.util.lang.Identifier;
import pascal.hardscript.util.print.NotepadWriter;

import java.util.*;
import java.util.stream.Collectors;

public class VerilogDumper {
    private static final Set<String> VERILOG_KEYWORDS;

    static {
        VERILOG_KEYWORDS = new HashSet<>();
        VERILOG_KEYWORDS.addAll(Arrays.asList(
                "always", "and", "assign", "automatic", "begin", "buf", "bufif0", "bufif1",
                "case", "casex", "casez", "cell", "cmos", "config", "deassign", "default",
                "defparam", "design", "disable", "edge", "else", "end", "endcase", "endconfig",
                "endfunction", "endgenerate", "endmodule", "endprimitive", "endspecify", "endtable", "endtask", "event",
                "for", "force", "forever", "fork", "function", "generate", "genvar", "highz0",
                "highz1", "if", "ifnone", "incdir", "include", "initial", "inout", "input",
                "instance", "integer", "join", "large", "liblist", "library", "localparam", "macromodule",
                "medium", "module", "nand", "negedge", "nmos", "nor", "noshowcancelled", "not",
                "notif0", "notif1", "or", "output", "parameter", "pmos", "posedge", "primitive",
                "pull0", "pull1", "pulldown", "pullup", "pulsestyle_onevent", "pulsestyle_ondetect", "rcmos", "real",
                "realtime", "reg", "release", "repeat", "rnmos", "rpmos", "rtran", "rtranif0",
                "rtranif1", "scalared", "showcancelled", "signed", "small", "specify", "specparam", "strong0",
                "strong1", "supply0", "supply1", "table", "task", "time", "tran", "tranif0",
                "tranif1", "tri", "tri0", "tri1", "triand", "trior", "trireg", "unsigned",
                "use", "uwire", "vectored", "wait", "wand", "weak0", "weak1", "while",
                "wire", "wor", "xnor", "xor"
        ));
    }

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

    public static String escape(String str) {
        if (str.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
            if (!VERILOG_KEYWORDS.contains(str)) {
                return str;
            }
        }
        return "\\" + str + " ";
    }

    private static String baseStringOf(DataRef ref) {
        return switch (ref) {
            case WireRefSelect select -> baseStringOf(select.base());
            case WireRef wire -> wire.name();
            case Constant _ -> null;
        };
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
        dumpIOPorts(module.getInputs(), module.getOutputs());
        notepad.newLine();
        notepad.decIndent();
        notepad.println(");");
        notepad.incIndent();
        defineModuleWires(module);
        defineModuleRegisters(module);
        module.getInstantiations().forEach(this::dumpInstantiation);
        module.getAssigns().forEach(this::dumpAssign);
        dumpRegisters(module.getAllRegisters());
        notepad.decIndent();
        notepad.println("endmodule");
        notepad.newLine();
    }

    private void defineModuleRegisters(VModule module) {
        for (var reg : module.getAllRegisters()) {
            var wire = reg.getTrueReg();
            notepad.print("reg " + typeOf(wire) + idOf(wire.id()) + ";");
            if (wire.id().getSourceLoc() != null) {
                notepad.println(" // @(" + wire.id().getSourceLoc() + ")");
            } else {
                notepad.newLine();
            }
        }
    }

    private void defineModuleWires(VModule module) {
        Set<String> inputSet = new HashSet<>();
        Set<String> outputSet = new HashSet<>();
        module.getInputs().stream()
                .map(VerilogDumper::baseStringOf)
                .filter(Objects::nonNull)
                .forEach(inputSet::add);
        module.getOutputs().stream()
                .map(VerilogDumper::baseStringOf)
                .filter(Objects::nonNull)
                .forEach(outputSet::add);
        for (var wire : module.getAllWires()) {
            String heading = "wire";
            if (inputSet.contains(wire.id().name())) {
                heading = "input";
            } else if (outputSet.contains(wire.id().name())) {
                heading = "output";
            }
            notepad.print(heading + " " + typeOf(wire) + idOf(wire.id()) + ";");
            if (wire.id().getSourceLoc() != null) {
                notepad.println(" // @(" + wire.id().getSourceLoc() + ")");
            } else {
                notepad.newLine();
            }
        }
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
            default -> {
                var delimiter = " " + BuiltinComponent.VERILOG_OP.get(assign.builtin()) + " ";
                notepad.print(String.join(delimiter, rhsList));
            }
        }
        notepad.println(";");
    }

    private void dumpInstantiation(Instantiation inst) {
        notepad.print(instNameOf(inst.getModule()));
        notepad.print(" " + escape("$inst_" + inst.getInstIndex()) + "(");
        dumpIOPorts(inst.getInputs(), inst.getOutputs());
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
        return switch (ref) {
            case Constant constant -> constant.value().toString();
            case WireRefSelect access -> dumpDataRef(access.base()) + access.selector().toString();
            case WireRef wireRef -> {
                String result = "";
                if (wireRef.instance() != null) {
                    result += "$inst_" + wireRef.instance().getInstIndex() + ".";
                }
                result += escape(wireRef.name());
                yield result;
            }
        };
    }

    private String idOf(Identifier id) {
        return escape(id.name());
    }

    private String instNameOf(VModule module) {
        return escape(module.getId().name() + "_" + module.getDefIndex());
    }
}
