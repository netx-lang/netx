package pascal.hardscript.hson;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class AbstractHFrontend<T> {
    protected static final ObjectMapper mapper = new ObjectMapper();
    protected JsonNode json;

    protected AbstractHFrontend(JsonNode json) {
        this.json = json;
    }

    public abstract T parse();
}
