package pascal.netx.simulator.frontend;

import com.fasterxml.jackson.databind.JsonNode;
import pascal.netx.simulator.model.Circuit;
import pascal.netx.simulator.model.ClockNode;
import pascal.netx.simulator.model.WireNode;
import pascal.netx.util.lang.Identifier;
import pascal.netx.util.type.Pair;

import static pascal.netx.util.hson.FrontendUtils.*;

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

    Pair<Identifier, Integer> parseIdentifier(JsonNode node) {
        var literal = node.get("id").textValue().split("@");
        return Pair.of(Identifier.of(literal[0]).setSourceLoc(sourceLocOf(node)), Integer.parseInt(literal[1]));
    }
}
