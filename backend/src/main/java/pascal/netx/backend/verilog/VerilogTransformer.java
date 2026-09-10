package pascal.netx.backend.verilog;

import com.fasterxml.jackson.databind.JsonNode;
import pascal.netx.backend.*;
import pascal.netx.util.exception.ShallNotReachHereError;
import pascal.netx.util.lang.BitVector;
import pascal.netx.util.lang.BuiltinComponent;
import pascal.netx.util.nxon.NxonType;
import pascal.netx.util.print.NotepadWriter;

import java.util.*;

public final class VerilogTransformer extends AbstractNxonTransformer {

    private static final String BAR = "=".repeat(30);

    public static final Set<String> VERILOG_KEYWORDS;

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

    public static String escape(String str) {
        if (!VERILOG_KEYWORDS.contains(str)) {
            return str;
        }
        return "\\" + str + " ";
    }

    private final HashSet<Integer> globalIn = new HashSet<>();
    private final HashSet<Integer> globalOut = new HashSet<>();
    private final List<String> extraDeclarations = new ArrayList<>();
    private final List<String> extraAssignments = new ArrayList<>();
    private final Map<WireReference, Integer> wireWidths = new HashMap<>();
    private final Map<WireReference, String> registerTemps = new HashMap<>();

    public VerilogTransformer(NotepadWriter notepad, JsonNode json) {
        super(notepad, json);
    }

    private static String parseWidth(int width) {
        if (width == 1) {
            return "";
        }
        return String.format("[%d:0] ", width - 1);
    }

    @Override
    public void parse() {
        var node = json.get("design");
        notepad.print("module ");
        prepareGlobalIO(node);
        collectWireWidths(node);
        collectRegisterTemps(node);
        notepad.newLine();
        notepad.println("/*%s DATA %s*/".formatted(BAR, BAR));
        notepad.newLine();
        extraDeclarations.forEach(notepad::println);
        parseComponent(node, ParseMode.DATA);
        notepad.newLine();
        notepad.println("/*%s CONNECTION %s*/".formatted(BAR, BAR));
        notepad.newLine();
        extraAssignments.forEach(notepad::println);
        parseComponent(node, ParseMode.CONNECTION);
        notepad.println("endmodule");
        notepad.flush();
    }

    private void prepareGlobalIO(JsonNode node) {
        notepad.print(escape(json.get("design").get("id").textValue()));
        notepad.println("(");
        notepad.incIndent();
        List<String> io = new ArrayList<>();
        processIOPorts(node, "input", io, globalIn);
        processIOPorts(node, "output", io, globalOut);
        notepad.println(String.join(", ", io.stream().distinct().toList()));
        notepad.decIndent();
        notepad.println(");");
    }

    private void processIOPorts(JsonNode node, String portType, List<String> io, HashSet<Integer> globalSet) {
        NxonParseUtils.streamOf(node, portType)
                .map(JsonNode::textValue)
                .map(Reference::from)
                .forEach(ref -> {
                    if (ref instanceof WireReference wireRef && wireRef.hasSelector()) {
                        String tempName = "io" + wireRef.dump().replaceAll("[\\[\\].:]", "_");
                        addTempPort(portType, io, tempName, parseSelectorWidth(wireRef.selector()), wireRef.dump());
                    } else if (ref instanceof ValueReference valueRef) {
                        int width = BitVector.parse(valueRef.constant()).size();
                        String tempName = "io_cons" + extraDeclarations.size();
                        addTempPort(portType, io, tempName, width, valueRef.dump());
                    } else {
                        io.add(ref.dump());
                        if (ref instanceof WireReference dataRef) {
                            globalSet.add(dataRef.id());
                        }
                    }
                });
    }

    private void addTempPort(String portType, List<String> io, String tempName, int width, String target) {
        io.add(tempName);
        extraDeclarations.add("%s wire %s%s;".formatted(portType, parseWidth(width), tempName));
        if (portType.equals("input")) {
            extraAssignments.add("assign %s = %s;".formatted(target, tempName));
        } else {
            extraAssignments.add("assign %s = %s;".formatted(tempName, target));
        }
    }

    private int parseSelectorWidth(String selector) {
        String content = selector.replace("[", "").replace("]", "");
        if (content.contains(":")) {
            String[] parts = content.split(":");
            int msb = Integer.parseInt(parts[0].trim());
            int lsb = Integer.parseInt(parts[1].trim());
            return Math.abs(msb - lsb) + 1;
        }
        return 1;
    }

    private void parseComponent(JsonNode node, ParseMode mode) {
        printComponentHeader(node, mode);
        notepad.incIndent();
        parseMembers(node, mode);
        notepad.decIndent();
        printComponentTail(node);
    }

    private void printComponentHeader(JsonNode node, ParseMode mode) {
        switch (mode) {
            case DATA -> notepad.println("/* %s; %s */".formatted(node.get("id"), node.get("loc")));
            case CONNECTION -> {
                notepad.println("/*");
                notepad.incIndent();
                notepad.print("COMPONENT ");
                notepad.println("%s; %s".formatted(node.get("id"), node.get("loc")));
                notepad.println("input: %s".formatted(String.join(", ",
                        NxonParseUtils.streamOf(node, "input").map(JsonNode::textValue).toList())));
                notepad.println("output: %s".formatted(String.join(", ",
                        NxonParseUtils.streamOf(node, "output").map(JsonNode::textValue).toList())));
                printAttributes(node);
                notepad.decIndent();
                notepad.println("*/");
            }
        }
    }

    private void printComponentTail(JsonNode node) {
        notepad.println("/* END %s; %s */".formatted(node.get("id"), node.get("loc")));
    }

    private void printAttributes(JsonNode node) {
        if (node.has("attribute")) {
            notepad.print("attribute: { ");
            var iter = node.get("attribute").fields();
            while (iter.hasNext()) {
                var entry = iter.next();
                notepad.print("%s: %s".formatted(entry.getKey(), entry.getValue()));
                if (iter.hasNext()) {
                    notepad.print(", ");
                }
            }
            notepad.println(" }");
        }
    }

    private void parseMembers(JsonNode node, ParseMode mode) {
        if (node.has("members")) {
            for (var sub : node.get("members")) {
                switch (NxonType.typeOf(sub)) {
                    case WIRE -> {
                        if (mode == ParseMode.DATA) {
                            parseWire(sub);
                        }
                    }
                    case CLOCK -> {
                        if (mode == ParseMode.DATA) {
                            parseClock(sub);
                        }
                    }
                    case BUILTIN -> parseBuiltin(sub, mode);
                    case COMPONENT -> parseComponent(sub, mode);
                }
            }
        }
    }

    private void collectWireWidths(JsonNode node) {
        if (!node.has("members")) {
            return;
        }
        for (var sub : node.get("members")) {
            switch (NxonType.typeOf(sub)) {
                case WIRE -> wireWidths.put(
                        ((WireReference) Reference.from(sub.get("id").textValue())).withoutSelector(),
                        sub.get("width").intValue()
                );
                case CLOCK -> wireWidths.put(
                        ((WireReference) Reference.from(sub.get("id").textValue())).withoutSelector(), 1
                );
                case BUILTIN, COMPONENT -> collectWireWidths(sub);
            }
        }
    }

    private void collectRegisterTemps(JsonNode node) {
        if (!node.has("members")) {
            return;
        }
        for (var sub : node.get("members")) {
            switch (NxonType.typeOf(sub)) {
                case BUILTIN -> {
                    var builtin = BuiltinComponent.of(sub.get("id").textValue());
                    if (builtin == BuiltinComponent.REGISTER || builtin == BuiltinComponent.ASYNC_REGISTER) {
                        var ref = ((WireReference) Reference.from(sub.get("output").get(0).textValue())).withoutSelector();
                        declareRegisterTemp(ref);
                    }
                    collectRegisterTemps(sub);
                }
                case COMPONENT -> collectRegisterTemps(sub);
                case WIRE, CLOCK -> { /* ignored */ }
            }
        }
    }

    private void parseWire(JsonNode node) {
        var ref = (WireReference) Reference.from(node.get("id").textValue());
        if (globalIn.contains(ref.id())) {
            notepad.print("input ");
        } else if (globalOut.contains(ref.id())) {
            notepad.print("output ");
        }
        notepad.print("wire ");
        notepad.print(parseWidth(node.get("width").intValue()));
        notepad.print(ref.dump());
        notepad.print("; // ");
        notepad.print("%s:".formatted(node.get("loc").textValue()));
        notepad.println(ref.name());
    }

    private void parseClock(JsonNode node) {
        var ref = (WireReference) Reference.from(node.get("id").textValue());
        if (globalIn.contains(ref.id())) {
            notepad.print("input ");
        } else if (globalOut.contains(ref.id())) {
            notepad.print("output ");
        }
        notepad.print("wire ");
        notepad.print(ref.dump());
        notepad.print("; // CLOCK ");
        notepad.print("%s:".formatted(node.get("loc").textValue()));
        notepad.println(ref.name());
    }

    private void parseBuiltin(JsonNode node, ParseMode mode) {
        parseMembers(node, mode);
        if (mode == ParseMode.CONNECTION) {
            var builtin = BuiltinComponent.of(node.get("id").textValue());
            var input = getArgList(node, "input");
            var output = getArgList(node, "output");
            switch (builtin) {
                case REGISTER -> parseRegister(node, input);
                case ASYNC_REGISTER -> parseAsyncRegister(node, input);
                default -> parseCombinational(builtin, output, input);
            }
            notepad.println(" // %s".formatted(node.get("loc").textValue()));
        }
    }

    private void parseRegister(JsonNode node, List<String> input) {
        boolean pos_edge = node.get("attribute").get("pos_edge").textValue().equals("true");
        String sensitive = pos_edge ? "posedge " : "negedge ";
        String tempReg = registerTargetOf(node.get("output").get(0).textValue());
        notepad.print("always @(%s%s) ".formatted(sensitive, input.get(0)));
        notepad.print("%s <= %s;".formatted(tempReg, input.get(1)));
    }

    private void parseAsyncRegister(JsonNode node, List<String> input) {
        boolean pos_edge = node.get("attribute").get("pos_edge").textValue().equals("true");
        boolean high_rst = node.get("attribute").get("high_rst").textValue().equals("true");
        String rst_value = node.get("attribute").get("rst_value").textValue();
        String clk_sensitive = pos_edge ? "posedge " : "negedge ";
        String rst_sensitive = high_rst ? "posedge " : "negedge ";
        String tempReg = registerTargetOf(node.get("output").get(0).textValue());
        notepad.println("always @(%s%s or %s%s) begin".formatted(clk_sensitive, input.get(0), rst_sensitive, input.get(1)));
        notepad.incIndent();
        notepad.print("if (%s%s) ".formatted(high_rst ? "" : "~", input.get(1)));
        notepad.println("%s <= %s;".formatted(tempReg, rst_value));
        notepad.println("else %s <= %s;".formatted(tempReg, input.get(2)));
        notepad.decIndent();
        notepad.print("end");
    }

    private String registerTargetOf(String target) {
        var ref = (WireReference) Reference.from(target);
        return declareRegisterTemp(ref.withoutSelector()) + ref.selector();
    }

    private String declareRegisterTemp(WireReference ref) {
        return registerTemps.computeIfAbsent(ref, key -> {
            Integer width = wireWidths.get(key);
            if (width == null) {
                throw new ShallNotReachHereError();
            }
            String tempReg = "__reg_" + key.dump().replaceAll("[^A-Za-z0-9_]", "_");
            extraDeclarations.add("reg %s%s;".formatted(parseWidth(width), tempReg));
            extraAssignments.add("assign %s = %s;".formatted(key.dump(), tempReg));
            return tempReg;
        });
    }

    private void parseCombinational(BuiltinComponent builtin, List<String> output, List<String> input) {
        notepad.print("assign %s = ".formatted(output.getFirst()));
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
            case UCAST, TO, AS_CLK, AS_BIT -> notepad.print(input.getFirst());
            case NOR, NAND, XNOR -> {
                var delimiter = switch (builtin) {
                    case NOR -> " | ";
                    case NAND -> " & ";
                    case XNOR -> " ^ ";
                    default -> throw new ShallNotReachHereError();
                };
                notepad.print("~(");
                notepad.print(String.join(delimiter, input));
                notepad.print(")");
            }
            default -> {
                var delimiter = " %s ".formatted(BuiltinComponent.VERILOG_OP.get(builtin));
                notepad.print(String.join(delimiter, input));
            }
        }
        notepad.print(";");
    }

    private List<String> getArgList(JsonNode node, String field) {
        return NxonParseUtils.streamOf(node, field)
                .map(JsonNode::textValue)
                .map(Reference::from)
                .map(Reference::dump)
                .toList();
    }

    enum ParseMode {
        DATA, CONNECTION
    }
}
