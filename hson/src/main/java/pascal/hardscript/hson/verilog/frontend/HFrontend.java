package pascal.hardscript.hson.verilog.frontend;

import com.fasterxml.jackson.databind.JsonNode;
import pascal.hardscript.hson.AbstractHFrontend;
import pascal.hardscript.hson.verilog.model.VModule;

public class HFrontend extends AbstractHFrontend<VModule> {
    private final VModule module = VModule.of("$top", null);
    private final VManager manager;

    private HFrontend(JsonNode json) {
        super(json);
        manager = new VManager(Integer.parseUnsignedInt(json.get("dataCount").asText()));
    }

    public VModule getModule() {
        return module;
    }

    public VManager getManager() {
        return manager;
    }

    @Override
    public VModule parse() {
        var design = json.get("design");
        new ScopeParser(this).run(design);
        new RelationParser(this).run(design);
        return module;
    }
}
