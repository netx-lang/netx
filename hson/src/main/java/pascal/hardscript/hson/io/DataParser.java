package pascal.hardscript.hson.io;

import com.fasterxml.jackson.databind.JsonNode;
import pascal.hardscript.hson.model.Circuit;
import pascal.hardscript.hson.model.ClockNode;
import pascal.hardscript.hson.model.WireNode;
import pascal.hardscript.hson.model.StreamNode;

import static pascal.hardscript.hson.io.FrontendUtils.*;

public final class DataParser {
    private final Circuit circuit;

    public DataParser(Circuit circuit) {
        this.circuit = circuit;
    }

    public void run(JsonNode root) {
        parse(root);
    }

    private void parse(JsonNode node) {
        switch (typeOf(node)) {
            case WIRE -> circuit.addVertex(parseWire(node));
            case CLOCK -> circuit.addVertex(parseClock(node));
            case COMPONENT -> forEachMember(node, this::parse);
        }
    }

    private StreamNode parseClock(JsonNode node) {
        return ClockNode.of(identifierOf(node));
    }

    private WireNode parseWire(JsonNode node) {
        return WireNode.of(identifierOf(node), widthOf(node));
    }

    private int widthOf(JsonNode node) {
        return node.get("width").intValue();
    }
}
