package pascal.netx.compiler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import pascal.netx.compiler.io.Configuration;
import pascal.netx.compiler.io.NXBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestUtils {
    public static void test(String expected, String actual) {
        var builder = NXBuilder.from(Configuration.asDefault());
        assertEquals(
                eliminateSource(builder.load(expected).compileAndDump()),
                eliminateSource(builder.load(actual).compileAndDump())
        );
    }

    private static JsonNode eliminateSource(JsonNode node) {
        if (node.has("loc")) {
            ((ObjectNode) node).put("loc", "0:0");
        }
        for (JsonNode child : node) {
            eliminateSource(child);
        }
        return node;
    }
}
