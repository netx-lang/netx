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
        RelationNode relation;
        if (id.string().equals("REGISTER")) {
            relation = parseRegister(node, id);
        } else {
            relation = ComputationNode.of(id);
        }
        circuit.addHyperEdge(relation,
                resolveNodes(node, "input"),
                resolveNodes(node, "output"));
        forEachMember(node, this::parseSubComponents);
    }

    private RelationNode parseRegister(JsonNode node, Identifier id) {
        RelationNode relation;
        var attribute = node.get("attribute");
        var clock = (ClockNode) reolveNode(attribute.get("clock"));
        boolean isPosTrigger = Boolean.parseBoolean(attribute.get("isPosTrigger").textValue());
        relation = RegisterNode.of(id, clock, isPosTrigger);
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
