package pascal.hardscript.hson.io;

import com.fasterxml.jackson.databind.JsonNode;
import pascal.hardscript.hson.model.*;
import pascal.hardscript.util.exception.ShallNotReachHereError;
import pascal.hardscript.util.lang.BitVector;
import pascal.hardscript.util.lang.Identifier;

import java.util.List;

import static pascal.hardscript.hson.io.FrontendUtils.*;

public final class RelationParser {
    private final Circuit circuit;
    private final DataManager manager;

    public RelationParser(Frontend frontend) {
        this.circuit = frontend.getCircuit();
        this.manager = frontend.getManager();
    }

    public void run(JsonNode root) {
        resolveNodes(root, "input").forEach(node -> circuit.addInput((DataNode) node));
        resolveNodes(root, "output").forEach(node -> circuit.addOutput((DataNode) node));
        parse(root);
    }

    private void parse(JsonNode node) {
        switch (typeOf(node)) {
            case Node.Type.CLOCK, WIRE -> {
                return;
            }
        }

        Node relation;
        var inputs = resolveNodes(node, "input");
        var outputs = resolveNodes(node, "output");
        switch (typeOf(node)) {
            case Node.Type.COMPONENT -> {
                var id = Identifier.of(node.get("id").textValue());
                relation = ComputationNode.of(id);
                forEachMember(node, this::parse);
            }
            case Node.Type.REGISTER -> {
                int width = outputs.getFirst().getWidth();
                relation = parseRegister(node, width);
            }
            default -> throw new ShallNotReachHereError();
        }
        circuit.addHyperEdge(relation, inputs, outputs);
    }

    private Node parseRegister(JsonNode node, int width) {
        Node relation;
        var attribute = node.get("attribute");
        var clock = (ClockNode) resolveNode(attribute.get("clock"));
        boolean isPosTrigger = Boolean.parseBoolean(attribute.get("isPosTrigger").textValue());
        relation = RegisterNode.of(width, clock, isPosTrigger);
        return relation;
    }

    private List<DataNode> resolveNodes(JsonNode node, String field) {
        return streamOf(node, field).map(this::resolveNode).toList();
    }

    private DataNode resolveNode(JsonNode node) {
        var parser = buildParser(node.textValue());
        var context = parser.data();
        if (context.identifier() != null) {
            var index = Integer.parseUnsignedInt(context.identifier().index.getText());
            return manager.resolve(index);
        } else {
            var value = BitVector.parse(
                    context.constant().literal.getText().substring(2),
                    Integer.parseUnsignedInt(context.constant().size.getText()), 16);
            var result = ConstantNode.of(value);
            circuit.addVertex(result);
            return result;
        }
    }
}
