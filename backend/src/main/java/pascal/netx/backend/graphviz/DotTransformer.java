package pascal.netx.backend.graphviz;

import com.fasterxml.jackson.databind.JsonNode;
import pascal.netx.backend.AbstractNxonTransformer;
import pascal.netx.backend.Reference;
import pascal.netx.backend.ValueReference;
import pascal.netx.backend.WireReference;
import pascal.netx.util.lang.BuiltinComponent;
import pascal.netx.util.print.NotepadWriter;
import pascal.netx.util.type.Pair;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static pascal.netx.backend.NxonParseUtils.arrayOf;
import static pascal.netx.backend.NxonParseUtils.quote;
import static pascal.netx.util.nxon.NxonType.typeOf;

public final class DotTransformer extends AbstractNxonTransformer {

    private static final String DOTTED_EDGE_STYLE = " [dir=none style=dotted constraint=false]";
    private static final String EDGE_STYLE = " [arrowsize=0.5 color=\"Navy\" penwidth=1.5]";
    private static final String VALUE_NODE_STYLE = " [style=filled fillcolor=\"SeaShell\" shape=plain]";
    private static final String IN_WIRE_STYLE = " [style=filled fillcolor=\"SeaShell\" shape=cds]";
    private static final String OUT_WIRE_STYLE = " [style=filled fillcolor=\"SeaShell\" shape=cds orientation=-180]";
    private static final String BLOCK_TABLE_BGCOLOR = "SeaShell";
    private static final String PORT_TABLE_BGCOLOR = "Bisque";
    private static final String BLOCK_STYLE = "margin=\"0,0.2\" shape=none";
    private final int depthThreshold;

    private final Map<Integer, String> tempIn = new HashMap<>();
    private final Map<Integer, List<String>> tempOut = new HashMap<>();
    private final Map<Integer, JsonNode> wireDef = new HashMap<>();
    private final Map<Integer, List<String>> selections = new HashMap<>();

    private final Set<Integer> inDefined = new HashSet<>();
    private final Set<Integer> outDefined = new HashSet<>();

    private final PendingConnection connection = PendingConnection.create();

    private final Supplier<Integer> componentIndexer = new AtomicInteger(0)::incrementAndGet;
    private final Supplier<Integer> constantIndexer = new AtomicInteger(0)::incrementAndGet;

    public DotTransformer(NotepadWriter notepad, JsonNode json, int depth) {
        super(notepad, json);
        this.depthThreshold = depth;
    }

    static String dotDefOfOutWire(WireReference ref) {
        return "%s[label=%s]%s".formatted(wireDotOf(ref.id(), false), quote("%s@%d".formatted(ref.name(), ref.id())), OUT_WIRE_STYLE);
    }

    static private String componentDotOf(int index) {
        return quote(String.format("c_%d", index));
    }

    static private String wireDotOf(int index, boolean isInSide) {
        return quote("w_%d%s".formatted(index, isInSide ? "i" : "o"));
    }

    static private String dirOf(boolean isInSide) {
        return isInSide ? ":e" : ":w";
    }

    @Override
    public void parse() {
        int total = json.get("total").intValue();
        double ranksep = Math.clamp(total / 200.0 * depthThreshold, 0.5, 10.0);
        notepad.println("digraph {");
        notepad.incIndent();
        notepad.println("rankdir=\"RL\";");
        notepad.println(String.format("graph [fontname = \"monospace\" compound=true ranksep=%.1f];", ranksep));
        notepad.println("node [fontname = \"monospace\"]");
        notepad.println("edge [fontname = \"monospace\"]");
        notepad.println("concentrate=false;");
        collectWireDef(json.get("design"), 1);
        printComponent(json.get("design"), 1);
        printConnection();
        notepad.decIndent();
        notepad.println("}");
        notepad.flush();
    }

    private void collectWireDef(JsonNode node, int depth) {
        var identifier = node.get("id").textValue();
        if (depth <= depthThreshold && !BuiltinComponent.isBuiltin(identifier)) {
            if (node.has("members")) {
                var iter = arrayOf(node, "members").elements();
                while (iter.hasNext()) {
                    var sub = iter.next();
                    switch (typeOf(sub)) {
                        case COMPONENT, BUILTIN -> collectWireDef(sub, depth + 1);
                        case WIRE, CLOCK -> {
                            var ref = (WireReference) Reference.from(sub.get("id").textValue());
                            wireDef.put(ref.id(), sub);
                        }
                    }
                }
            }
        }
    }

    private void printConnection() {
        printDefinedWireConnections();
        printSelectionConnections();
        printTemporaryConnections();
        printPendingConnections();
    }

    private void printDefinedWireConnections() {
        for (var def : wireDef.values()) {
            var ref = (WireReference) Reference.from(def.get("id").textValue());
            if (!inDefined.contains(ref.id())) {
                notepad.println(dotDefOfInWire(ref));
            }

            if (!outDefined.contains(ref.id())) {
                notepad.println(dotDefOfOutWire(ref));
            }
            notepad.println(
                    "%s%s -> %s%s%s".formatted(wireDotOf(ref.id(), true), dirOf(false), wireDotOf(ref.id(), false), dirOf(true), DOTTED_EDGE_STYLE));
        }
    }

    private void printSelectionConnections() {
        for (var entry : selections.entrySet()) {
            var id = entry.getKey();
            if (wireDef.containsKey(id)) {
                for (var dot : defineSelection(entry)) {
                    notepad.println("%s -> %s%s".formatted(dot, wireDotOf(id, false), DOTTED_EDGE_STYLE));
                    notepad.println("%s%s -> %s%s".formatted(wireDotOf(id, true), dirOf(false), dot, DOTTED_EDGE_STYLE));
                }
            } else if (tempIn.containsKey(id)) {
                for (var dot : defineSelection(entry)) {
                    notepad.println("%s -> %s%s".formatted(dot, tempIn.get(id), DOTTED_EDGE_STYLE));
                }
            }
        }
    }

    private void printTemporaryConnections() {
        for (var entry : tempIn.entrySet()) {
            var id = entry.getKey();
            if (!wireDef.containsKey(id)) {
                var source = entry.getValue();
                if (tempOut.containsKey(id)) {
                    for (var target : tempOut.get(entry.getKey())) {
                        printEdge(source, target);
                    }
                }
            }
        }
    }

    private void printPendingConnections() {
        for (var edge : connection.wireOut()) {
            if (!edge.first().isEmpty()) {
                printEdge(edge.first(), edge.second());
            }
        }
        for (var edge : connection.wireIn()) {
            if (!edge.first().isEmpty()) {
                printEdge(edge.second(), edge.first());
            }
        }
    }

    private List<String> defineSelection(Map.Entry<Integer, List<String>> entry) {
        notepad.println("{");
        notepad.incIndent();
        notepad.println("rank=same");
        var dots = new ArrayList<String>();
        for (var def : entry.getValue()) {
            notepad.println(def);
            dots.add(def.substring(0, def.indexOf("[label") - 1));
        }
        notepad.decIndent();
        notepad.println("}");
        return dots;
    }

    private void printEdge(String source, String target) {
        notepad.println("%s -> %s%s".formatted(source, target, EDGE_STYLE));
    }

    private void printComponent(JsonNode node, int depth) {
        int index = componentIndexer.get();
        var identifier = node.get("id").textValue();

        if (depth > depthThreshold || BuiltinComponent.isBuiltin(identifier)) {
            connection.merge(printBlock(node, index));
        } else {
            printComponentAsSubgraph(node, index, depth);
        }
    }

    private void printComponentAsSubgraph(JsonNode node, int index, int depth) {
        notepad.println(String.format("subgraph \"cluster.%d\" {", index));
        notepad.incIndent();

        printSubgraphStyle(node);

        if (node.has("members")) {
            var iter = arrayOf(node, "members").elements();
            while (iter.hasNext()) {
                var sub = iter.next();
                switch (typeOf(sub)) {
                    case COMPONENT, BUILTIN -> printComponent(sub, depth + 1);
                }
            }
        }

        notepad.decIndent();
        notepad.println("}");
    }

    private void printSubgraphStyle(JsonNode node) {
        notepad.println("label=%s".formatted(quote(node.get("id").textValue())));
        notepad.println("labeljust = c");
        notepad.println("labelloc = t");
        notepad.println("pencolor = seagreen");
        notepad.println("penwidth = 2");
    }

    private PendingConnection printBlock(JsonNode node, int index) {
        notepad.print(componentDotOf(index));
        notepad.print("[label=<");
        notepad.print(String.format("<table bgcolor=\"%s\">", BLOCK_TABLE_BGCOLOR));
        notepad.print("<tr>");
        var connection = PendingConnection.create();
        connection.merge(printPort(node, index, IO.OUTPUT));
        notepad.print(String.format("<td>%s</td>", node.get("id").textValue()));
        connection.merge(printPort(node, index, IO.INPUT));
        notepad.print("</tr>");
        notepad.print("</table>");
        notepad.print(String.format("> %s]", BLOCK_STYLE));
        notepad.newLine();

        for (var def : connection.define()) {
            notepad.println(def);
        }
        return connection;
    }

    private PendingConnection printPort(JsonNode node, int index, IO io) {
        notepad.print("<td>");
        notepad.print(String.format("<table bgcolor=\"%s\">", PORT_TABLE_BGCOLOR));

        var connection = PendingConnection.create();
        var componentDotRef = componentDotOf(index);
        int portIndex = 0;
        var iter = arrayOf(node, io.asField()).elements();
        while (iter.hasNext()) {
            var port = iter.next();
            var portDotRef = "%s_%d".formatted(io.asField(), portIndex++);
            notepad.print("<tr>");
            notepad.print(String.format("<td port=%s>•</td>", quote(portDotRef)));
            notepad.print("</tr>");
            connection.merge(connectPort(io, port, componentDotRef, portDotRef));
        }
        notepad.print("</table>");
        notepad.print("</td>");

        return connection;
    }

    private PendingConnection connectPort(IO io, JsonNode port, String componentDotRef, String portDotRef) {
        var result = PendingConnection.create();
        var globalRef = String.format("%s:%s:%s", componentDotRef, portDotRef, io.asDirection());

        switch (Reference.from(port.textValue())) {
            case ValueReference ref -> handleValueReference(io, ref, result, globalRef);
            case WireReference ref -> handleDataReference(io, ref, result, globalRef);
        }
        return result;
    }

    private void handleValueReference(IO io, ValueReference ref, PendingConnection result, String globalRef) {
        var dot = quote("v_%d".formatted(constantIndexer.get()));
        result.define().add("%s[label=%s]%s".formatted(dot, quote(ref.dump()), VALUE_NODE_STYLE));
        switch (io) {
            case INPUT -> result.wireOut().add(new Pair<>(dot + dirOf(false), globalRef));
            case OUTPUT -> result.wireOut().add(new Pair<>(dot + dirOf(true), globalRef));
        }
    }

    private void handleDataReference(IO io, WireReference ref, PendingConnection result, String globalRef) {
        if (ref.hasSelector()) {
            var dot = quote("s_%d%s".formatted(ref.id(), ref.selector()));
            switch (io) {
                case INPUT -> result.wireOut().add(new Pair<>(dot + dirOf(false), globalRef));
                case OUTPUT -> result.wireIn().add(new Pair<>(dot + dirOf(true), globalRef));
            }
            selections.computeIfAbsent(ref.id(), _ -> new ArrayList<>()).add(
                    "%s [label=%s]%s".formatted(dot, quote(ref.name() + ref.selector()), VALUE_NODE_STYLE)
            );
        } else {
            switch (io) {
                case OUTPUT -> handleOutputConnection(ref, result, globalRef);
                case INPUT -> handleInputConnection(ref, result, globalRef);
            }
        }
    }

    private void handleOutputConnection(WireReference ref, PendingConnection result, String globalRef) {
        if (wireDef.containsKey(ref.id())) {
            result.wireIn().add(new Pair<>(
                    wireDotOf(ref.id(), true) + dirOf(true),
                    globalRef));
            if (!inDefined.contains(ref.id())) {
                result.define().add(dotDefOfInWire(ref));
                inDefined.add(ref.id());
            }
        } else {
            result.wireIn().add(new Pair<>("", ""));
            tempIn.put(ref.id(), globalRef);
        }
    }

    private String dotDefOfInWire(WireReference ref) {
        return "%s[label=%s]%s".formatted(
                wireDotOf(ref.id(), true),
                quote("%s@%d\\l%s".formatted(
                        ref.name(), ref.id(), wireDef.get(ref.id()).get("loc").textValue())),
                IN_WIRE_STYLE
        );
    }

    private void handleInputConnection(WireReference ref, PendingConnection result, String globalRef) {
        if (wireDef.containsKey(ref.id())) {
            result.wireOut().add(new Pair<>(
                    wireDotOf(ref.id(), false) + dirOf(false),
                    globalRef));
            if (!outDefined.contains(ref.id())) {
                result.define().add(dotDefOfOutWire(ref));
                outDefined.add(ref.id());
            }
        } else {
            result.wireOut().add(new Pair<>("", ""));
            tempOut.computeIfAbsent(ref.id(), _ -> new ArrayList<>()).add(globalRef);
        }
    }

    enum IO {
        INPUT, OUTPUT;

        String asDirection() {
            return switch (this) {
                case INPUT -> "e";
                case OUTPUT -> "w";
            };
        }

        String asField() {
            return this.name().toLowerCase();
        }
    }

    private record PendingConnection(
            List<Pair<String, String>> wireIn,
            List<Pair<String, String>> wireOut,
            List<String> define) {

        public static PendingConnection create() {
            return new PendingConnection(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        }

        public void merge(PendingConnection other) {
            this.wireIn.addAll(other.wireIn);
            this.wireOut.addAll(other.wireOut);
            this.define.addAll(other.define);
        }
    }
}
