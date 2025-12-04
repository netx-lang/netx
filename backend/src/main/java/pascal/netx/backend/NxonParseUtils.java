package pascal.netx.backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class NxonParseUtils {
    public static ArrayNode arrayOf(JsonNode node, String field) {
        return (ArrayNode) node.get(field);
    }

    public static Stream<JsonNode> streamOf(JsonNode node, String field) {
        var array = arrayOf(node, field);
        return StreamSupport.stream(array.spliterator(), false);
    }

    public static String quote(String str) {
        return "\"" + str + "\"";
    }
}
