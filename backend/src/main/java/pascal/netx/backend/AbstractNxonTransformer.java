package pascal.netx.backend;

import com.fasterxml.jackson.databind.JsonNode;

public abstract class AbstractNxonTransformer<T> {
    protected JsonNode json;

    protected AbstractNxonTransformer(JsonNode json) {
        this.json = json;
    }

    public abstract T parse();
}
