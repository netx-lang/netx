package pascal.hardscript.hson;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.LinkedList;
import java.util.List;

public abstract class AbstractHFrontend<T> {
    protected static final ObjectMapper mapper = new ObjectMapper();
    protected List<JsonNode> defNodes = new LinkedList<>();

    protected AbstractHFrontend(JsonNode json) {
        json.elements().forEachRemaining(defNodes::add);
    }

    public abstract T parse();
}
