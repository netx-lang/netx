package pascal.hardscript.hson.io;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import pascal.hardscript.hson.model.Circuit;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public class Frontend {
    private static final ObjectMapper mapper = new ObjectMapper();
    private final Circuit circuit = new Circuit();
    private final DataManager manager;
    private final JsonNode json;

    private Frontend(JsonNode node) {
        this.json = node;
        this.manager = new DataManager(json.get("dataCount").intValue());
    }

    public static Frontend load(JsonNode node) {
        return new Frontend(node);
    }

    public static Frontend load(InputStream inputStream) throws IOException {
        return new Frontend(mapper.readTree(inputStream));
    }

    public static Frontend load(String string) throws IOException {
        return new Frontend(mapper.readTree(string));
    }

    public static Frontend loadPath(Path path) throws IOException {
        return new Frontend(mapper.readTree(path.toFile()));
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
