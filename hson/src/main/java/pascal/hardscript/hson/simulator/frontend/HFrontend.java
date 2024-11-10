package pascal.hardscript.hson.simulator.frontend;

import com.fasterxml.jackson.databind.JsonNode;
import pascal.hardscript.hson.AbstractHFrontend;
import pascal.hardscript.hson.simulator.model.Circuit;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public final class HFrontend extends AbstractHFrontend<Circuit> {
    private final Circuit circuit = new Circuit();
    private final DataManager manager;

    private HFrontend(JsonNode node) {
        super(node);
        this.manager = new DataManager(json.get("dataCount").intValue());
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

    public Circuit getCircuit() {
        return circuit;
    }

    public DataManager getManager() {
        return manager;
    }

    public Circuit parse() {
        var design = json.get("design");
        new DataParser(this).run(design);
        new RelationParser(this).run(design);
        return circuit;
    }
}
