package pascal.hardscript.hson.io;

import com.fasterxml.jackson.databind.JsonNode;
import pascal.hardscript.hson.model.Circuit;
import pascal.hardscript.hson.model.ClockNode;
import pascal.hardscript.hson.model.StatefulNode;
import pascal.hardscript.hson.model.StatelessNode;

import static pascal.hardscript.hson.io.FrontendUtils.forEachMember;
import static pascal.hardscript.hson.io.FrontendUtils.identifierOf;

public class InstanceParser {
    private final Circuit circuit;

    public InstanceParser(Circuit circuit) {
        this.circuit = circuit;
    }

    public void parse(JsonNode node) {
        switch (FrontendUtils.typeOf(node)) {
            case WIRE -> circuit.addVertex(parseWire(node));
            case CLOCK -> circuit.addVertex(parseClock(node));
            case COMPONENT -> forEachMember(node, this::parse);
        }
    }

    public StatefulNode parseClock(JsonNode node) {
        return ClockNode.of(identifierOf(node));
    }

    public StatelessNode parseWire(JsonNode node) {
        return StatelessNode.of(identifierOf(node), widthOf(node));
    }

    public int widthOf(JsonNode node) {
        return node.get("width").intValue();
    }
}
