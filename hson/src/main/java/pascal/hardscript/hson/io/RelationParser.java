package pascal.hardscript.hson.io;

import com.fasterxml.jackson.databind.JsonNode;
import pascal.hardscript.hson.antlr.HsonDataParser;
import pascal.hardscript.hson.model.*;
import pascal.hardscript.hson.model.selector.BitSelector;
import pascal.hardscript.hson.model.selector.PartSelector;
import pascal.hardscript.hson.model.selector.Selector;
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
            return parseDataNode(context);
        } else if (context.constant() != null) {
            return parseConstant(context);
        } else {
            return parseWireSelect(context.select());
        }
    }

    private DataNode parseDataNode(HsonDataParser.DataContext context) {
        var index = Integer.parseUnsignedInt(context.identifier().index.getText());
        return manager.resolve(index);
    }

    private ConstantNode parseConstant(HsonDataParser.DataContext context) {
        HsonDataParser.ConstantContext ctx = context.constant();
        var result = ConstantNode.of(parseConstantValue(ctx));
        circuit.addVertex(result);
        return result;
    }

    private DataNode parseWireSelect(HsonDataParser.SelectContext ctx) {
        var index = Integer.parseUnsignedInt(ctx.identifier().index.getText());
        DataNode base = manager.resolve(index), result = base;
        for (var selectCtx : ctx.selector()) {
            var selector = parseSelector(selectCtx);
            result = DataSelectNode.of(base, selector);
            circuit.addVertex(result);
            circuit.addEdge(base, result);
            base = result;
        }
        return result;
    }

    static Selector parseSelector(HsonDataParser.SelectorContext ctx) {
        return switch (ctx) {
            case HsonDataParser.BitSelectorContext sub -> BitSelector.of(
                    Integer.parseUnsignedInt(sub.index.getText()));
            case HsonDataParser.PartSelectorContext sub -> PartSelector.of(
                    Integer.parseUnsignedInt(sub.from.getText()),
                    Integer.parseUnsignedInt(sub.to.getText()));
            default -> throw new ShallNotReachHereError();
        };
    }

    static BitVector parseConstantValue(HsonDataParser.ConstantContext ctx) {
        BitVector value;
        if (ctx.HEX() != null) {
            var literal = ctx.getText().toLowerCase().split("'h");
            value = BitVector.parse(literal[1], Integer.parseUnsignedInt(literal[0]), 16);
        } else {
            value = BitVector.fromInt(Integer.parseInt(ctx.getText()));
        }
        return value;
    }
}
