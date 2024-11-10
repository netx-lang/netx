package pascal.hardscript.hson.verilog;

import com.fasterxml.jackson.databind.JsonNode;
import pascal.hardscript.hson.AbstractHFrontend;
import pascal.hardscript.hson.verilog.model.VModule;
import pascal.hardscript.hson.verilog.model.Wire;
import pascal.hardscript.util.lang.BuiltinComponent;

import static pascal.hardscript.hson.util.FrontendUtils.*;

public final class VConverter extends AbstractHFrontend<VModule> {
    private final VModule design = new VModule("$design");
    private final Wire[] wires;

    private VConverter(JsonNode json) {
        super(json);
        wires = new Wire[json.get("dataCount").intValue()];
    }

    @Override
    public VModule parse() {
        parse(json.get("design"), design);

        return design;
    }

    private void parse(JsonNode node, VModule parent) {
        switch (typeOf(node)) {
            case COMPONENT -> parseComponent(parent, node);
            case REGISTER -> parseRegister(parent, node);
            case CLOCK -> parseWire(parent, node, 1);
            case WIRE -> parseWire(parent, node, widthOf(node));
        }
    }

    private void parseComponent(VModule parent, JsonNode node) {
        String name = node.get("id").textValue();
        if (BuiltinComponent.isBuiltin(name)) {
            forEachMember(node, n -> parse(n, parent));
        } else {
            var result = VModule.of(name);
            forEachMember(node, n -> parse(n, result));
            parent.addSubModule(result);
        }
    }

    private void parseRegister(VModule parent, JsonNode node) {
        forEachMember(node, n -> parse(n, parent));
    }

    private void parseWire(VModule parent, JsonNode node, int width) {
        var id = parseIdentifier(node);
        var result = Wire.of(id.first(), width);
        wires[id.second()] = result;
        parent.addWire(result);
    }

    private void fill(JsonNode node, VModule parent) {
        switch (typeOf(node)) {
            case COMPONENT -> {
                String name = node.get("id").textValue();
                if (BuiltinComponent.isBuiltin(name)) {

                }
            }
            case REGISTER -> {

            }
        }
    }

//    private Wire resolveNode(JsonNode node) {
//        var parser = buildParser(node.textValue());
//        var context = parser.data();
//        if (context.identifier() != null) {
//            var index = Integer.parseUnsignedInt(context.identifier().index.getText());
//            return wires[index];
//        } else if (context.constant() != null) {
//
//        } else {
//
//        }
//    }
}
