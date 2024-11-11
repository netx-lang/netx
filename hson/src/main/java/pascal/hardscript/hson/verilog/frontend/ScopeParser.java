package pascal.hardscript.hson.verilog.frontend;

import com.fasterxml.jackson.databind.JsonNode;
import pascal.hardscript.hson.verilog.model.VModule;
import pascal.hardscript.hson.verilog.model.Wire;
import pascal.hardscript.util.lang.BuiltinComponent;

import static pascal.hardscript.hson.util.FrontendUtils.*;

public final class ScopeParser {
    private final VModule module;
    private final VManager manager;

    public ScopeParser(HFrontend frontend) {
        this.module = frontend.getModule();
        this.manager = frontend.getManager();
    }

    public void run(JsonNode root) {
        parse(root, module);
    }

    private void parse(JsonNode node, VModule parent) {
        switch (typeOf(node)) {
            case WIRE -> parseWire(parent, node, widthOf(node));
            case CLOCK -> parseWire(parent, node, 1);
            case COMPONENT -> parseComponent(node, parent);
        }
    }

    private void parseWire(VModule parent, JsonNode node, int width) {
        var id = parseIdentifier(node);
        var result = Wire.of(id.first(), width);
        manager.addData(id.second(), result);
        parent.addWire(result);
    }

    private void parseComponent(JsonNode node, VModule parent) {
        String name = node.get("id").textValue();
        if (!BuiltinComponent.isBuiltin(name)) {
            var module = VModule.of(name, parent);
            manager.addModule(node, module);
            forEachMember(node, n -> parse(n, module));
        } else {
            forEachMember(node, n -> parse(n, parent));
        }
    }
}
