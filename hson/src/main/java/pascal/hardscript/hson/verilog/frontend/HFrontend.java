package pascal.hardscript.hson.verilog.frontend;

import com.fasterxml.jackson.databind.JsonNode;
import pascal.hardscript.hson.AbstractHFrontend;
import pascal.hardscript.hson.verilog.model.VModule;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public class HFrontend extends AbstractHFrontend<VModule> {
    private final VModule module = VModule.of("$top");
    private final VManager manager;

    private HFrontend(JsonNode json) {
        super(json);
        manager = new VManager(Integer.parseUnsignedInt(json.get("dataCount").asText()));
    }

    public static HFrontend load(JsonNode node) {
        return new HFrontend(node);
    }

    public static HFrontend load(InputStream inputStream) throws IOException {
        return new HFrontend(mapper.readTree(inputStream));
    }

    public static HFrontend load(String string) throws IOException {
        return new HFrontend(mapper.readTree(string));
    }

    public static HFrontend loadPath(Path path) throws IOException {
        return new HFrontend(mapper.readTree(path.toFile()));
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
