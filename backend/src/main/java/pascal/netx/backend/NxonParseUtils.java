package pascal.netx.backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class NxonParseUtils {
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

    public static ArrayNode arrayOf(JsonNode node, String field) {
        return (ArrayNode) node.get(field);
    }

    public static Stream<JsonNode> streamOf(JsonNode node, String field) {
        var array = arrayOf(node, field);
        return StreamSupport.stream(array.spliterator(), false);
    }

    public static String escape(String str) {
        if (!VERILOG_KEYWORDS.contains(str)) {
            return str;
        }
        return "\\" + str + " ";
    }

    public static String quote(String str) {
        return "\"" + str + "\"";
    }
}
