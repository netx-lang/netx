package pascal.hardscript.hson.simulator.frontend;

import com.fasterxml.jackson.databind.JsonNode;
import pascal.hardscript.hson.simulator.model.Circuit;
import pascal.hardscript.hson.simulator.model.ClockNode;
import pascal.hardscript.hson.simulator.model.WireNode;

import static pascal.hardscript.hson.util.FrontendUtils.*;

public final class DataParser {
    private final Circuit circuit;
    private final DataManager manager;

    public DataParser(HFrontend frontend) {
        this.circuit = frontend.getCircuit();
        this.manager = frontend.getManager();
    }

    public void run(JsonNode root) {
        parse(root);
    }

    private void parse(JsonNode node) {
        switch (typeOf(node)) {
            case WIRE -> circuit.addVertex(parseWire(node));
            case CLOCK -> circuit.addVertex(parseClock(node));
            default -> forEachMember(node, this::parse);
        }
    }

    private ClockNode parseClock(JsonNode node) {
        var id = parseIdentifier(node);
        var result = ClockNode.of(id.first());
        manager.addNode(id.second(), result);
        return result;
    }

    private WireNode parseWire(JsonNode node) {
        var id = parseIdentifier(node);
        var result = WireNode.of(id.first(), widthOf(node));
        manager.addNode(id.second(), result);
        return result;
    }
}
