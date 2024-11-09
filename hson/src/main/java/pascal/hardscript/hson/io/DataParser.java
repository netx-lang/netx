package pascal.hardscript.hson.io;

import com.fasterxml.jackson.databind.JsonNode;
import pascal.hardscript.hson.model.Circuit;
import pascal.hardscript.hson.model.ClockNode;
import pascal.hardscript.hson.model.WireNode;
import pascal.hardscript.util.lang.Identifier;
import pascal.hardscript.util.lang.SourceLoc;
import pascal.hardscript.util.type.Pair;

import static pascal.hardscript.hson.io.FrontendUtils.*;

public final class DataParser {
    private final Circuit circuit;
    private final DataManager manager;

    public DataParser(Frontend frontend) {
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

    private int widthOf(JsonNode node) {
        return node.get("width").intValue();
    }

    private static Pair<Identifier, Integer> parseIdentifier(JsonNode node) {
        var parser = buildParser(node.get("id").textValue());
        var context = parser.identifier();

        var id = Identifier.of(context.name.getText()).setSourceLoc(
                SourceLoc.of(
                        Integer.parseUnsignedInt(context.line.getText()),
                        Integer.parseUnsignedInt(context.column.getText())
                )
        );
        var index = Integer.parseUnsignedInt(context.index.getText());
        return Pair.of(id, index);
    }
}
