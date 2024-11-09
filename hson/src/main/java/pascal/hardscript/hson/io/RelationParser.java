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
            case Node.Type.CLOCK, WIRE -> {
                return;
            }
        }

        var inputs = resolveNodes(node, "input");
        var outputs = resolveNodes(node, "output");
        switch (typeOf(node)) {
            case Node.Type.COMPONENT -> {
                Node relation = ComputationNode.of(Identifier.of(node.get("id").textValue()));
                circuit.addHyperEdge(relation, inputs, outputs);
            }
            case Node.Type.REGISTER  -> {
                var register = parseRegister(node, outputs.getFirst(), inputs.getFirst());
                circuit.addRegister(register);
            }
            default -> throw new ShallNotReachHereError();
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
