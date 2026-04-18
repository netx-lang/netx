package pascal.netx.backend.optimization;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import pascal.netx.backend.AbstractNxonTransformer;
import pascal.netx.backend.Reference;
import pascal.netx.backend.ValueReference;
import pascal.netx.backend.WireReference;
import pascal.netx.backend.optimization.model.*;
import pascal.netx.util.exception.ShallNotReachHereError;
import pascal.netx.util.lang.BitVector;
import pascal.netx.util.nxon.NxonType;
import pascal.netx.util.print.NotepadWriter;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static pascal.netx.backend.NxonParseUtils.streamOf;

public final class GraphTransformer extends AbstractNxonTransformer {
    private final Graph graph = new Graph();

    public GraphTransformer(NotepadWriter notepad, JsonNode json) {
        super(notepad, json);
    }

    private final Map<ValueReference, Constant> constRefs = new HashMap<>();
    private final Map<WireReference, Vertex> wireRefs = new HashMap<>();

    private final Map<WireReference, JsonNode> wireDefs = new HashMap<>();
    private final Map<JsonNode, Component> compDefs = new HashMap<>();
    private final Map<JsonNode, String> hierarchyPaths = new IdentityHashMap<>();

    @Override
    public void parse() {
        indexHierarchy(json.get("design"), "");
        buildWires();
        buildComponents();
        buildConnections();
        graph.setGlobalIn(extractIOVertices(streamOf(json.get("design"), "input")));
        graph.setGlobalOut(extractIOVertices(streamOf(json.get("design"), "output")));
    }

    private List<Vertex> extractIOVertices(Stream<JsonNode> stream) {
        return stream.map(JsonNode::textValue)
                .map(Reference::from)
                .map(WireReference.class::cast)
                .map(wireRefs::get)
                .toList();
    }

    private void indexHierarchy(JsonNode node, String currentPath) {
        var nextPath = currentPath;
        if (node != null && node.has("type")) {
            var type = node.get("type").textValue();
            if ("component".equals(type) || "builtin".equals(type)) {
                var id = node.has("id") ? node.get("id").textValue() : "";
                nextPath = currentPath.isBlank() ? id : currentPath + "/" + id;
            }
        }
        hierarchyPaths.put(node, nextPath);
        if (node != null && node.has("members")) {
            for (var member : node.get("members")) {
                indexHierarchy(member, nextPath);
            }
        }
    }

    public void rewrite() {
        new Rewriter(graph).rewrite();
    }

    public Measurer.Metrics measure() {
        return new Measurer(graph).evaluate();
    }

    public void saturate(double areaWeight, double delayWeight) {
        new Saturator(graph, areaWeight, delayWeight).saturate();
    }

    private void buildWires() {
        var inputs = streamOf(json.get("design"), "input")
                .map(JsonNode::textValue)
                .map(Reference::from)
                .collect(Collectors.toSet());

        iterate(json.get("design"), node -> {
            switch (NxonType.typeOf(node)) {
                case WIRE, CLOCK -> {
                    var label = node.get("id").textValue();
                    var ref = (WireReference) Reference.from(label);
                    if (inputs.contains(ref)) {
                        int width = definedWithOf(node);
                        var vertex = graph.factory().wire(label, width);
                        graph.recordLocation(vertex, locOf(node));
                        graph.recordName(vertex, ref.name());
                        graph.recordHierarchyPath(vertex, hierarchyOf(node));
                        wireRefs.put(ref, vertex);
                    }
                    wireDefs.put(ref, node);
                }
                case BUILTIN, COMPONENT -> { /* ignored */ }
            }
        });
    }

    private static int definedWithOf(JsonNode node) {
        if (NxonType.typeOf(node) == NxonType.CLOCK) {
            return 1;
        }
        return node.get("width").intValue();
    }

    private void buildComponents() {
        iterate(json.get("design"), node -> {
            if (Objects.requireNonNull(NxonType.typeOf(node)) == NxonType.BUILTIN) {
                var output = node.get("output").get(0);
                var ref = (WireReference) Reference.from(output.textValue());
                int width;
                if (wireDefs.containsKey(ref)) {
                    width = wireDefs.get(ref).get("width").intValue();
                } else {
                    width = parseSelector(ref.selector()).width();
                }

                String label = node.get("id").textValue();
                Component vertex;
                if (label.equals("REGISTER")) {
                    boolean isPosEdge = node.get("attribute").get("pos_edge").textValue().equals("true");
                    vertex = graph.factory().register(width, isPosEdge);
                } else {
                    vertex = graph.factory().component(label, width);
                }
                graph.recordLocation(vertex, locOf(node));
                graph.recordName(vertex, ref.withoutSelector().name());
                graph.recordHierarchyPath(vertex, hierarchyOf(node));
                compDefs.put(node, vertex);
                if (!ref.selector().isEmpty()) {
                    buildWriteSelect(ref, vertex, locOf(node));
                } else {
                    wireRefs.put(ref, vertex);
                }
            }
        });
    }

    private void buildConnections() {
        for (var entry : definedSelects.entrySet()) {
            var wireRef = entry.getKey();
            var selects = entry.getValue();
            selects.sort(Comparator.comparing(WriteSelect::selector).reversed());
            int width = wireDefs.get(wireRef).get("width").intValue();
            var concat = graph.factory().component(Component.Label.CONCAT, width);
            for (var select : selects) {
                graph.inheritLocations(select, concat);
                graph.inheritNames(select, concat);
                graph.addEdge(select, concat);
            }
            wireRefs.put(wireRef, concat);
        }

        iterate(json.get("design"), node -> {
            if (Objects.requireNonNull(NxonType.typeOf(node)) == NxonType.BUILTIN) {
                var vertex = compDefs.get(node);
                boolean isEq = node.get("id").textValue().equals("TO");
                for (var input: node.get("input")) {
                    var inVertex = switch (Reference.from(input.textValue())) {
                        case WireReference ref -> {
                            if (wireRefs.containsKey(ref)) {
                                var result = wireRefs.get(ref);
                                graph.addEdge(result, vertex);
                                yield result;
                            } else {
                                var base = Objects.requireNonNull(wireRefs.get(ref.withoutSelector()));
                                Selector selector = parseSelector(ref.selector());
                                var select = graph.factory().readSelect(selector);
                                graph.recordLocation(select, locOf(node));
                                graph.recordName(select, ref.withoutSelector().name());
                                graph.recordHierarchyPath(select, hierarchyOf(node));
                                graph.addEdge(base, select);
                                graph.addEdge(select, vertex);
                                wireRefs.put(ref, select);
                                yield select;
                            }
                        }
                        case ValueReference ref -> {
                            var constant = buildConstant(ref);
                            graph.addEdge(constant, vertex);
                            yield constant;
                        }
                    };

                    if (isEq) {
                        graph.manager().replace(vertex, inVertex);
                    }
                }
            }
        });
    }

    private final Map<WireReference, List<WriteSelect>> definedSelects = new HashMap<>();

    private void buildWriteSelect(WireReference ref, Component vertex, String loc) {
        var key = ref.withoutSelector();
        if (wireRefs.containsKey(key)) {
            throw new ShallNotReachHereError();
        }

        var label = wireDefs.get(key).get("id").textValue();
        Selector selector = parseSelector(ref.selector());
        var select = graph.factory().writeSelect(label, selector);
        graph.recordLocation(select, loc);
        graph.recordName(select, key.name());
        graph.recordHierarchyPath(select, hierarchyOf(wireDefs.get(key)));
        definedSelects.computeIfAbsent(key, _ -> new LinkedList<>()).add(select);
        graph.addEdge(vertex, select);
    }

    private String locOf(JsonNode node) {
        if (node != null && node.has("loc")) {
            return node.get("loc").textValue();
        }
        return "";
    }

    private String hierarchyOf(JsonNode node) {
        return hierarchyPaths.getOrDefault(node, "");
    }

    Selector parseSelector(String selector) {
        String literal = selector.substring(1, selector.length() - 1);
        if (literal.contains(":")) {
            var array = literal.split(":");
            int msb = Integer.parseInt(array[0]);
            int lsb = Integer.parseInt(array[1]);
            return new Selector(msb, lsb);
        } else {
            int index = Integer.parseInt(literal);
            return new Selector(index, index);
        }
    }

    private Constant buildConstant(ValueReference ref) {
        return constRefs.computeIfAbsent(ref, k -> {
            BitVector value = BitVector.parse(k.constant());
            return graph.factory().constant(value);
        });
    }

    public JsonNode dumpNxon() {
        return new NxonDumper().dump();
    }

    public void dumpDot() {
        graph.dumpDot(notepad);
        notepad.flush();
        notepad.close();
    }

    private void iterate(JsonNode node, Consumer<JsonNode> action) {
        action.accept(node);
        if (node.has("members")) {
            for (var member : node.get("members")) {
                iterate(member, action);
            }
        }
    }

    private final class NxonDumper {
        private static final ObjectMapper MAPPER = new ObjectMapper();

        private final Map<Vertex, String> references = new HashMap<>();
        private final List<WireDef> definitions = new LinkedList<>();
        private final List<JsonNode> components = new LinkedList<>();

        private final Supplier<Integer> indexer = new AtomicInteger(0)::getAndIncrement;

        JsonNode dump() {
            for (var vertex : graph.globalOut()) {
                dump(graph.eqClassOf(vertex).getAny());
            }
            canonicalize();
            var root = MAPPER.createObjectNode();
            var design = MAPPER.createObjectNode();

            design.put("type", "component");
            design.put("id", "top");
            design.put("loc", locOf(json.get("design")));

            var members = MAPPER.createArrayNode();
            for (var def : definitions) {
                members.add(def.asNxon());
            }
            for (var component : components) {
                members.add(component);
            }

            var inputs = MAPPER.createArrayNode();
            var outputs = MAPPER.createArrayNode();
            for (var input : graph.globalIn()) {
                inputs.add(references.get(graph.eqClassOf(input).getAny()));
            }
            for (var output : graph.globalOut()) {
                outputs.add(references.get(graph.eqClassOf(output).getAny()));
            }
            design.set("input", inputs);
            design.set("output", outputs);

            design.set("members", members);

            root.put("total", definitions.size());
            root.set("top_input", json.get("top_input"));
            root.set("top_output", json.get("top_output"));
            root.set("design", design);
            return root;
        }

        void dump(Vertex vertex) {
            switch (vertex) {
                case Wire wire -> {
                    var name = hierarchicalNameOf(wire, wire.name().substring(0, wire.name().indexOf('@')));
                    var def = new WireDef(
                            name,
                            indexer.get(),
                            wire.outWidth(),
                            graph.locationOf(wire));
                    definitions.add(def);
                    references.put(wire, def.asReference());
                }
                case Constant constant -> references.put(constant, constant.name());
                case WriteSelect select -> {
                    assert graph.inputsOf(vertex).size() == 1;
                    var cls = graph.inputsOf(vertex).getFirst();
                    assert cls.isSingleton();
                    var input = cls.getAny();

                    if (!references.containsKey(input)) {
                        dump(input);
                    }
                    references.put(select, references.get(input));
                }
                case ReadSelect select -> {
                    assert graph.inputsOf(vertex).size() == 1;
                    var cls = graph.inputsOf(vertex).getFirst();
                    assert cls.isSingleton();
                    var input = cls.getAny();

                    if (!references.containsKey(input)) {
                        dump(input);
                    }
                    references.put(select, references.get(input) + select.selector().toString());
                }
                case Component component -> {
                    var node = MAPPER.createObjectNode();
                    var inputs = MAPPER.createArrayNode();
                    var outputs = MAPPER.createArrayNode();

                    node.put("type", "builtin");
                    node.put("id", component.name());
                    node.put("loc", graph.locationOf(component));

                    var name = hierarchicalNameOf(component, "$o");
                    var def = new WireDef(
                            name,
                            indexer.get(),
                            component.outWidth(),
                            graph.locationOf(component));
                    var ref = def.asReference();
                    outputs.add(ref);
                    definitions.add(def);
                    references.put(component, ref);

                    for (var cls : graph.inputsOf(vertex)) {
                        assert cls.isSingleton();
                        var input = cls.getAny();
                        if (!references.containsKey(input)) {
                            dump(input);
                        }
                        inputs.add(references.get(input));
                    }

                    node.set("input", inputs);
                    node.set("output", outputs);

                    if (component instanceof Register reg) {
                        var attribute = node.has("attribute") && node.get("attribute").isObject()
                                ? (com.fasterxml.jackson.databind.node.ObjectNode) node.get("attribute")
                                : MAPPER.createObjectNode();
                        if (reg.isPosEdge()) {
                            attribute.put("pos_edge", "true");
                        } else {
                            attribute.put("pos_edge", "false");
                        }
                        node.set("attribute", attribute);
                    }

                    components.add(node);
                }
            }
        }

        private String hierarchicalNameOf(Vertex vertex, String fallbackName) {
            var name = graph.preferredNameOf(vertex);
            if (name.isBlank()) {
                name = fallbackName;
            }

            var hierarchy = graph.hierarchyPathsOf(vertex).stream().findFirst().orElse("");
            if (hierarchy.isBlank()) {
                return name;
            }
            if (name.isBlank() || hierarchy.equals(name) || hierarchy.endsWith("/" + name)) {
                return hierarchy;
            }
            return hierarchy + "/" + name;
        }

        void canonicalize() {
            for (var entry : references.entrySet()) {
                var ref = Reference.from(entry.getValue());
                if (!(ref instanceof WireReference wireRef) || !wireRef.hasSelector()) {
                    continue;
                }
                var canonical = canonicalize(wireRef);
                entry.setValue(canonical.name() + "@" + canonical.id() + canonical.selector());
            }
            for (var component : components) {
                canonicalizeReferences(component.get("input"));
                canonicalizeReferences(component.get("output"));
            }
        }

        private WireReference canonicalize(WireReference ref) {
            var selectors = parseSelectors(ref.selector());
            if (selectors.size() <= 1) {
                return ref;
            }

            var current = selectors.getFirst();
            for (int i = 1; i < selectors.size(); i++) {
                current = inline(current, selectors.get(i));
            }
            return ref.withSelector(current.toString());
        }

        private List<Selector> parseSelectors(String selectorLiteral) {
            var selectors = new ArrayList<Selector>();
            for (int index = 0; index < selectorLiteral.length(); ) {
                int open = selectorLiteral.indexOf('[', index);
                if (open < 0) {
                    break;
                }
                int close = selectorLiteral.indexOf(']', open);
                if (close < 0) {
                    throw new ShallNotReachHereError();
                }
                selectors.add(parseSelector(selectorLiteral.substring(open, close + 1)));
                index = close + 1;
            }
            return selectors;
        }

        private Selector inline(Selector base, Selector nested) {
            if (nested.lsb() < 0 || nested.msb() >= base.width()) {
                throw new ShallNotReachHereError();
            }
            return new Selector(base.lsb() + nested.msb(), base.lsb() + nested.lsb());
        }

        private void canonicalizeReferences(JsonNode refs) {
            if (refs == null || !refs.isArray()) {
                return;
            }
            var array = (com.fasterxml.jackson.databind.node.ArrayNode) refs;
            for (int i = 0; i < array.size(); i++) {
                var ref = array.get(i);
                if (!ref.isTextual()) {
                    continue;
                }
                var parsed = Reference.from(ref.textValue());
                if (parsed instanceof WireReference wireRef && wireRef.hasSelector()) {
                    var canonical = canonicalize(wireRef);
                    array.set(i, MAPPER.getNodeFactory()
                            .textNode(canonical.name() + "@" + canonical.id() + canonical.selector()));
                }
            }
        }

        private record WireDef(String name,
                               int index,
                               int width,
                               String loc) {
            String asReference() {
                return name + "@" + index;
            }

            JsonNode asNxon() {
                var node = MAPPER.createObjectNode();
                node.put("type", "wire");
                node.put("id", asReference());
                node.put("width", width);
                node.put("loc", loc);
                return node;
            }
        }
    }
}
