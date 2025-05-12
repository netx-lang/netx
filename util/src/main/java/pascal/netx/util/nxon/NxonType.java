package pascal.netx.util.nxon;

import com.fasterxml.jackson.databind.JsonNode;

public enum NxonType {
    COMPONENT, BUILTIN, INSTANTIATE, CLOCK, WIRE;

    public static NxonType typeOf(JsonNode node) {
        return valueOf(node.get("type").textValue().toUpperCase());
    }
}
