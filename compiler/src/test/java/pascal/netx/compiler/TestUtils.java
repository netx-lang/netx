package pascal.netx.compiler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import pascal.netx.compiler.io.Configuration;
import pascal.netx.compiler.io.NXBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestUtils {
    public static void testBothCompactAndExpanded(String expected, String actual) {
        testCompact(expected, actual);
        testExpanded(expected, actual);
    }

    public static void testCompact(String expected, String actual) {
        var cBuilder = NXBuilder.from(Configuration.forCompactTest());
        assertEquals(
                eliminateSource(cBuilder.load(expected).compileAndDump()),
                eliminateSource(cBuilder.load(actual).compileAndDump())
        );
    }

    public static void testExpanded(String expected, String actual) {
        var eBuilder = NXBuilder.from(Configuration.forExpandedTest());
        assertEquals(
                eliminateSource(eBuilder.load(expected).compileAndDump()),
                eliminateSource(eBuilder.load(actual).compileAndDump())
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
