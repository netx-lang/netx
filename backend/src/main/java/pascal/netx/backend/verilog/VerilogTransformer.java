package pascal.netx.backend.verilog;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import pascal.netx.backend.AbstractNxonTransformer;
import pascal.netx.util.lang.BuiltinComponent;
import pascal.netx.util.nxon.NxonType;
import pascal.netx.util.print.NotepadWriter;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class VerilogTransformer extends AbstractNxonTransformer<Void> {
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
    private final HashSet<Integer> globalIn = new HashSet<>();
    private final HashSet<Integer> globalOut = new HashSet<>();

    public VerilogTransformer(NotepadWriter notepad, JsonNode json) {
        super(json);
        this.notepad = notepad;
    }

    private static ArrayNode arrayOf(JsonNode node, String field) {
        return (ArrayNode) node.get(field);
    }

    private static Stream<JsonNode> streamOf(JsonNode node, String field) {
        var array = arrayOf(node, field);
        return StreamSupport.stream(array.spliterator(), false);
    }

    private static String escape(String str) {
        if (!VERILOG_KEYWORDS.contains(str)) {
            return str;
        }
        return "\\" + str + " ";
    }

    private static String parseWidth(int width) {
        if (width == 1) {
            return "";
        }
        return String.format("[%d:0] ", width - 1);
    }

    @Override
    public Void parse() {
        var node = json.get("design");
        notepad.print("module ");
        prepareGlobalIO(node);
        parseComponent(node);
        notepad.println("endmodule");
        notepad.flush();
        return null;
    }

    private void prepareGlobalIO(JsonNode node) {
        notepad.print(escape(json.get("design").get("id").textValue()));
        notepad.println("(");
        notepad.incIndent();
        List<String> io = new ArrayList<>();
        processIOPorts(node, "input", io, globalIn);
        processIOPorts(node, "output", io, globalOut);
        notepad.println(String.join(", ", io));
        notepad.decIndent();
        notepad.println(");");
    }

    private void processIOPorts(JsonNode node, String portType, List<String> io, HashSet<Integer> globalSet) {
        streamOf(node, portType)
                .map(JsonNode::textValue)
                .map(Reference::from)
                .forEach(ref -> {
                    io.add(ref.dump());
                    globalSet.add(ref.id());
                });
    }

    private void parseComponent(JsonNode node) {
        printComponentHeader(node);
        notepad.incIndent();
        parseMembers(node);
        notepad.decIndent();
    }

    private void printComponentHeader(JsonNode node) {
        notepad.println("/*");
        notepad.incIndent();
        notepad.print("COMPONENT " );
        notepad.println(node.get("id") + "; " + node.get("loc"));
        notepad.println("input" + ": " + String.join(", ",
                streamOf(node, "input").map(JsonNode::textValue).toList()));
        notepad.println("output" + ": " + String.join(", ",
                streamOf(node, "output").map(JsonNode::textValue).toList()));
        printAttributes(node);
        notepad.decIndent();
        notepad.println("*/");
    }

    private void printAttributes(JsonNode node) {
        if (node.has("attribute")) {
            notepad.print("attribute: { ");
            var iter = node.get("attribute").fields();
            while (iter.hasNext()) {
                var entry = iter.next();
                notepad.print(entry.getKey() + ": " + entry.getValue());
                if (iter.hasNext()) {
                    notepad.print(", ");
                }
            }
            notepad.println(" }");
        }
    }

    private void parseMembers(JsonNode node) {
        if (node.has("members")) {
            for (var sub : node.get("members")) {
                switch (NxonType.typeOf(sub)) {
                    case WIRE -> parseWire(sub);
                    case CLOCK -> parseClock(sub);
                    case BUILTIN ->  parseBuiltin(sub);
                    case COMPONENT -> parseComponent(sub);
                }
            }
        }
    }

    private void parseWire(JsonNode node) {
        var ref = Reference.from(node.get("id").textValue());
        if (globalIn.contains(ref.id())) {
            notepad.print("input ");
        } else if (globalOut.contains(ref.id())) {
            notepad.print("output ");
        }
        notepad.print("wire ");
        notepad.print(parseWidth(node.get("width").intValue()));
        notepad.print(ref.dump());
        notepad.print("; // ");
        notepad.print(node.get("loc").textValue() + ":");
        notepad.println(ref.name());
    }

    private void parseClock(JsonNode node) {
        var ref = Reference.from(node.get("id").textValue());
        if (globalIn.contains(ref.id())) {
            notepad.print("input ");
        } else if (globalOut.contains(ref.id())) {
            notepad.print("output ");
        }
        notepad.print("wire ");
        notepad.print(ref.dump());
        notepad.print("; // CLOCK ");
        notepad.print(node.get("loc").textValue() + ":");
        notepad.println(ref.name());
    }

    private void parseBuiltin(JsonNode node) {
        parseMembers(node);
        var builtin = BuiltinComponent.of(node.get("id").textValue());
        var input = getArgList(node, "input");
        var output = getArgList(node, "output");
        if (builtin == BuiltinComponent.REGISTER) {
            parseRegister(node, output, input);
        } else {
            parseCombinational(builtin, output, input);
        }
        notepad.println(" // " + node.get("loc").textValue());
    }

    private void parseRegister(JsonNode node, List<String> output, List<String> input) {
        boolean pos_edge = node.get("attribute").get("pos_edge").textValue().equals("true");
        String sensitive = pos_edge ? "posedge " : "negedge ";
        notepad.print("always @(" + sensitive + input.getFirst() + ") ");
        notepad.print(output.getFirst() + " <= " + input.getFirst() + ";");
    }

    private void parseCombinational(BuiltinComponent builtin, List<String> output, List<String> input) {
        notepad.print("assign " + output.getFirst() + " = ");
        switch (builtin) {
            case NOT -> {
                notepad.print("~");
                notepad.print(input.getFirst());
            }
            case CONCAT -> {
                notepad.print("{");
                notepad.print(String.join(", ", input));
                notepad.print("}");
            }
            case CAST -> {
                notepad.print("$signed(");
                notepad.print(input.getFirst());
                notepad.print(")");
            }
            case UCAST, TO -> notepad.print(input.getFirst());
            default -> {
                var delimiter = " " + BuiltinComponent.VERILOG_OP.get(builtin) + " ";
                notepad.print(String.join(delimiter, input));
            }
        }
        notepad.print(";");
    }

    private List<String> getArgList(JsonNode node, String field) {
        return streamOf(node, field)
                .map(JsonNode::textValue)
                .map(Reference::from)
                .map(Reference::dump)
                .toList();
    }

    private record Reference(String name, Integer id, String selector, String constant) {
        static Reference from(String literal) {
            if (literal.contains("@")) {
                var parts = literal.split("@");
                if (parts[1].contains("[")) {
                    int index = parts[1].indexOf('[');
                    return new Reference(parts[0],
                            Integer.parseUnsignedInt(parts[1].substring(0, index)),
                            parts[1].substring(index), null);
                } else {
                    return new Reference(parts[0], Integer.parseUnsignedInt(parts[1]), "", null);
                }
            } else {
                return new Reference(null, null, null, literal);
            }
        }

        String dump() {
            if (constant == null) {
                return "_" + id + selector;
            }
            return constant;
        }
    }
}
