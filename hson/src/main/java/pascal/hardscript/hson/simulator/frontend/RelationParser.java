package pascal.hardscript.hson.simulator.frontend;

import com.fasterxml.jackson.databind.JsonNode;
import pascal.hardscript.hson.antlr.HsonDataParser;
import pascal.hardscript.hson.simulator.model.*;
import pascal.hardscript.hson.util.HsonType;
import pascal.hardscript.util.lang.Identifier;

import java.util.List;

import static pascal.hardscript.hson.util.FrontendUtils.*;

public final class RelationParser {
    private final Circuit circuit;
    private final DataManager manager;

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
            case HsonType.COMPONENT -> {
                Node relation = ComputationNode.of(Identifier.of(node.get("id").textValue()));
                circuit.addHyperEdge(relation, inputs, outputs);
            }
            case HsonType.REGISTER  -> {
                var register = parseRegister(node, outputs.getFirst(), inputs.getFirst());
                circuit.addRegister(register);
            }
        }
        forEachMember(node, this::parse);
    }

    private Register parseRegister(JsonNode node, DataNode state, DataNode next) {
        var attribute = node.get("attribute");
        var clock = (ClockNode) resolveNode(attribute.get("clock"));
        boolean isPosTrigger = Boolean.parseBoolean(attribute.get("isPosTrigger").textValue());
        return Register.of(state, next, clock, isPosTrigger);
    }

    private List<DataNode> resolveNodes(JsonNode node, String field) {
        return streamOf(node, field).map(this::resolveNode).toList();
    }

    private DataNode resolveNode(JsonNode node) {
        var parser = buildParser(node.textValue());
        var context = parser.data();
        if (context.identifier() != null) {
            return resolveDataNode(context.identifier());
        } else if (context.constant() != null) {
            return resolveConstant(context.constant());
        } else {
            return resolveWireSelect(context.select());
        }
    }

    private DataNode resolveDataNode(HsonDataParser.IdentifierContext context) {
        var index = Integer.parseUnsignedInt(context.index.getText());
        return manager.resolve(index);
    }

    private ConstantNode resolveConstant(HsonDataParser.ConstantContext context) {
        var result = ConstantNode.of(parseConstantValue(context));
        circuit.addVertex(result);
        return result;
    }

    private DataNode resolveWireSelect(HsonDataParser.SelectContext context) {
        var index = Integer.parseUnsignedInt(context.identifier().index.getText());
        DataNode base = manager.resolve(index), result;
        var selector = parseSelector(context.selector());
        result = DataSelectNode.of(base, selector);
        circuit.addVertex(result);
        circuit.addEdge(base, result);
        return result;
    }
}
