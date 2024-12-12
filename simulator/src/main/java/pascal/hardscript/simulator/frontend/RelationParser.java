package pascal.hardscript.simulator.frontend;

import com.fasterxml.jackson.databind.JsonNode;
import pascal.hardscript.simulator.model.*;
import pascal.hardscript.util.exception.ShallNotReachHereError;
import pascal.hardscript.util.hson.HsonType;
import pascal.hardscript.util.hson.selector.BitSelector;
import pascal.hardscript.util.hson.selector.PartSelector;
import pascal.hardscript.util.hson.selector.Selector;
import pascal.hardscript.util.lang.BitVector;
import pascal.hardscript.util.lang.BuiltinComponent;
import pascal.hardscript.util.lang.Identifier;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static pascal.hardscript.util.hson.FrontendUtils.*;

public final class RelationParser {
    private final Circuit circuit;
    private final DataManager manager;
    private final Map<Pattern, Function<Matcher, DataNode>> IDREF_PATTERN = Map.ofEntries(
            Map.entry(Pattern.compile("(.*)@(\\d+)"), this::resolvePlainData),
            Map.entry(Pattern.compile("(.*)@(\\d+)\\[(\\d+)]"), this::resolveBitSelectData),
            Map.entry(Pattern.compile("(.*)@(\\d+)\\[(\\d+):(\\d+)]"), this::resolvePartSelectData),
            Map.entry(Pattern.compile("(\\d+)'h([0-9a-fA-F]+)"), this::resolveBitVectorConstant),
            Map.entry(Pattern.compile("(\\d+)"), this::resolveIntConstant)
    );

    public RelationParser(HFrontend frontend) {
        this.circuit = frontend.getCircuit();
        this.manager = frontend.getManager();
    }

    public void run(JsonNode root) {
        resolveNodes(root, "input").forEach(circuit::addInput);
        resolveNodes(root, "output").forEach(circuit::addOutput);
        parse(root);
    }

    private void parse(JsonNode node) {
        switch (typeOf(node)) {
            case HsonType.CLOCK, WIRE -> {
                return;
            }
        }

        var inputs = resolveNodes(node, "input");
        var outputs = resolveNodes(node, "output");
        switch (typeOf(node)) {
            case COMPONENT -> {
                var id = Identifier.of(node.get("id").textValue());
                Node relation = ComputationNode.of(id);
                circuit.addHyperEdge(relation, inputs, outputs);
            }
            case BUILTIN -> {
                var id = Identifier.of(node.get("id").textValue());
                if (BuiltinComponent.isBuiltin(id) && BuiltinComponent.of(id) == BuiltinComponent.REGISTER) {
                    var register = parseRegister(node, outputs.getFirst(), inputs.getFirst());
                    circuit.addRegister(register);
                } else {
                    Node relation = ComputationNode.of(id);
                    circuit.addHyperEdge(relation, inputs, outputs);
                }
            }
        }
        forEachMember(node, this::parse);
    }

    private Register parseRegister(JsonNode node, DataNode state, DataNode next) {
        var attribute = node.get("attribute");
        var clock = (ClockNode) resolveNode(attribute.get("clock").textValue());
        boolean isPosTrigger = Boolean.parseBoolean(attribute.get("isPosTrigger").textValue());
        return Register.of(state, next, clock, isPosTrigger);
    }

    private List<DataNode> resolveNodes(JsonNode node, String field) {
        return streamOf(node, field).map(JsonNode::textValue).map(this::resolveNode).toList();
    }

    private DataNode resolveNode(String literal) {
        for (var entry : IDREF_PATTERN.entrySet()) {
            var matcher = entry.getKey().matcher(literal);
            if (matcher.matches()) {
                return entry.getValue().apply(matcher);
            }
        }
        throw new ShallNotReachHereError();
    }

    DataNode resolvePlainData(Matcher matcher) {
        int index = Integer.parseUnsignedInt(matcher.group(2));
        return manager.resolve(index);
    }

    DataSelectNode resolveBitSelectData(Matcher matcher) {
        DataNode base = resolvePlainData(matcher);
        Selector selector = BitSelector.of(Integer.parseUnsignedInt(matcher.group(3)));
        return addSelectNodeToGraph(base, selector);
    }

    DataSelectNode resolvePartSelectData(Matcher matcher) {
        DataNode base = resolvePlainData(matcher);
        Selector selector = PartSelector.of(
                Integer.parseUnsignedInt(matcher.group(3)),
                Integer.parseUnsignedInt(matcher.group(4)));
        return addSelectNodeToGraph(base, selector);
    }

    private DataSelectNode addSelectNodeToGraph(DataNode base, Selector selector) {
        var result = DataSelectNode.of(base, selector);
        circuit.addVertex(result);
        circuit.addEdge(base, result);
        return result;
    }

    DataNode resolveBitVectorConstant(Matcher matcher) {
        int size = Integer.parseUnsignedInt(matcher.group(1));
        var result = ConstantNode.of(BitVector.parse(matcher.group(2), size, 16));
        circuit.addVertex(result);
        return result;
    }

    DataNode resolveIntConstant(Matcher matcher) {
        int rep = Integer.parseUnsignedInt(matcher.group(1));
        var result = ConstantNode.of(BitVector.fromInt(rep));
        circuit.addVertex(result);
        return result;
    }
}
