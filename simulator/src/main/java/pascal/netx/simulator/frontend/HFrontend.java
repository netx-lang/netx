package pascal.netx.simulator.frontend;

import com.fasterxml.jackson.databind.JsonNode;
import pascal.netx.simulator.model.Circuit;
import pascal.netx.util.hson.AbstractHFrontend;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public final class HFrontend extends AbstractHFrontend<Circuit> {
    private final Circuit circuit = new Circuit();
    private final DataManager manager;

    private HFrontend(JsonNode node) {
        super(node);
        this.manager = new DataManager(json.get("totalData").intValue());
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

    public DataManager getManager() {
        return manager;
    }

    public Circuit getCircuit() {
        return circuit;
    }

    public Circuit parse() {
        var design = json.get("design");
        new DataParser(this).run(design);
        new RelationParser(this).run(design);
        return circuit;
    }
}
