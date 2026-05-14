package pascal.netx.compiler.io;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;

class NxonDumperUnitTest {
    @Test
    void testUnusedLocalFormalsAreNotDumpedAsMembers() {
        var code = """
                component top : [clk, rst, d] -> [q] {
                    wire clk of clock();
                    wire rst of bit(1);
                    wire d, q of bit(4);
                    q <> reg <> [clk, rst, d];
                }

                component reg : [clk, rst, d] -> [q] {
                    wire clk of clock();
                    wire rst of bit(1);
                    wire d, q of bit(4);
                    q <> REGISTER(true) <> [clk, d];
                }
                """;
        var dump = assertDoesNotThrow(() -> NXBuilder.from(Configuration.asDefault())
                .load(code)
                .compileAndDump());
        var reg = findComponent(dump.get("design"), "reg");
        assertNotNull(reg);

        var members = reg.get("members");
        assertNotNull(members);
        assertFalse(hasMemberIdStartingWith(members, "clk@"));
        assertFalse(hasMemberIdStartingWith(members, "rst@"));
        assertFalse(hasMemberIdStartingWith(members, "d@"));
        assertFalse(hasMemberIdStartingWith(members, "q@"));
    }

    private static JsonNode findComponent(JsonNode node, String id) {
        if (node == null || node.isMissingNode()) {
            return null;
        }
        if ("component".equals(textOf(node.get("type"))) && id.equals(textOf(node.get("id")))) {
            return node;
        }
        var members = node.get("members");
        if (members == null || !members.isArray()) {
            return null;
        }
        for (var member : members) {
            var found = findComponent(member, id);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    private static boolean hasMemberIdStartingWith(JsonNode members, String prefix) {
        return StreamSupport.stream(members.spliterator(), false)
                .filter(member -> "wire".equals(textOf(member.get("type")))
                        || "clock".equals(textOf(member.get("type"))))
                .map(member -> textOf(member.get("id")))
                .anyMatch(id -> id.startsWith(prefix));
    }

    private static String textOf(JsonNode node) {
        return node == null ? "" : node.asText();
    }
}
