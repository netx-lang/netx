package pascal.netx.verilog.frontend;

import com.fasterxml.jackson.databind.JsonNode;
import pascal.netx.util.nxon.AbstractHFrontend;
import pascal.netx.verilog.model.VModule;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static pascal.netx.util.nxon.FrontendUtils.*;

public class HFrontend extends AbstractHFrontend<List<VModule>> {
    private final Map<VModuleIndex, VModule> definitions = new HashMap<>();
    private final List<JsonNode> defNodes = new LinkedList<>();

    private HFrontend(JsonNode json) {
        super(json);
        json.elements().forEachRemaining(defNodes::add);
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

    @Override
    public List<VModule> parse() {
        List<VModule> result = new LinkedList<>();
        for (var node : defNodes) {
            var id = identifierOf(node);
            var defIndex = defIndexOf(node);
            definitions.put(VModuleIndex.of(id.name(), defIndex), VModule.of(id, defIndex));
        }
        for (var node : defNodes) {
            var id = identifierOf(node);
            var module = definitions.get(VModuleIndex.of(id.name(), defIndexOf(node)));
            new ScopeParser(this, node, module).parse();
            result.add(module);
        }
        return result;
    }

    public VModule resolveModuleDef(VModuleIndex index) {
        return definitions.get(index);
    }

}
