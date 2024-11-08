package pascal.hardscript.hson.io;

import com.fasterxml.jackson.databind.JsonNode;
import pascal.hardscript.hson.model.*;
import pascal.hardscript.util.lang.Identifier;

import java.util.List;

import static pascal.hardscript.hson.io.FrontendUtils.*;

public final class RelationParser {
    private final Circuit circuit;

    public RelationParser(Circuit circuit) {
        this.circuit = circuit;
    }

    public void run(JsonNode root) {
        resolveNodes(root, "input");
        resolveNodes(root, "output");
    }

    private void parse(JsonNode node) {
        var id = identifierOf(node);
        var inputs = resolveNodes(node, "input");
        var outputs = resolveNodes(node, "output");

        Node relation;
        if (id.string().equals("REGISTER")) {
            int width = ((DataNode) outputs.getFirst()).getWidth();
            relation = parseRegister(node, id, width);
        } else {
            relation = ComputationNode.of(id);
        }
        circuit.addHyperEdge(relation, inputs, outputs);
        forEachMember(node, this::parseSubComponents);
    }

    private Node parseRegister(JsonNode node, Identifier id, int width) {
        Node relation;
        var attribute = node.get("attribute");
        var clock = (ClockNode) reolveNode(attribute.get("clock"));
        boolean isPosTrigger = Boolean.parseBoolean(attribute.get("isPosTrigger").textValue());
        relation = RegisterNode.of(id, width, clock, isPosTrigger);
        return relation;
    }

    private List<Node> resolveNodes(JsonNode node, String field) {
        return streamOf(node, field).map(this::reolveNode).toList();
    }

    private Node reolveNode(JsonNode node) {
        return circuit.resolve(parseIdentifier(node.textValue()));
    }

    private void parseSubComponents(JsonNode member) {
        if (typeOf(member) == Node.Type.COMPONENT) {
            parse(member);
        }
    }
}
