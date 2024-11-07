package pascal.hardscript.hson.io;

import com.fasterxml.jackson.databind.JsonNode;
import pascal.hardscript.hson.model.Circuit;
import pascal.hardscript.hson.model.Node;

import java.util.List;

import static pascal.hardscript.hson.io.FrontendUtils.*;

public final class RelationParser {
    private final Circuit circuit;

    public RelationParser(Circuit circuit) {
        this.circuit = circuit;
    }

    public void parse(JsonNode node) {
        circuit.addHyperEdge(identifierOf(node),
                resolveNodes(node, "input"),
                resolveNodes(node, "output"));
        forEachMember(node, this::parseSubComponents);
    }

    private List<Node> resolveNodes(JsonNode node, String field) {
        return streamOf(node, field)
                .map(FrontendUtils::identifierOf)
                .map(circuit::resolve).toList();
    }

    private void parseSubComponents(JsonNode member) {
        if (typeOf(member) == Node.Type.COMPONENT) {
            parse(member);
        }
    }
}
