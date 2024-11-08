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
    private final JsonNode json;

    private Frontend(JsonNode node) {
        this.json = node;
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

    public Circuit parse() {
        new DataParser(circuit).run(json);
        new RelationParser(circuit).run(json);
        return circuit;
    }
}
