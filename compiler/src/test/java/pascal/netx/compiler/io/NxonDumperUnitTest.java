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

    private static boolean hasToAlias(JsonNode members, String inputPrefix, String outputPrefix, String outputSuffix) {
        return StreamSupport.stream(members.spliterator(), false)
                .filter(member -> "builtin".equals(textOf(member.get("type")))
                        && "TO".equals(textOf(member.get("id"))))
                .anyMatch(member -> {
                    var input = member.get("input");
                    var output = member.get("output");
                    if (input == null || output == null || input.isEmpty() || output.isEmpty()) {
                        return false;
                    }
                    var inputRef = textOf(input.get(0));
                    var outputRef = textOf(output.get(0));
                    return (inputPrefix.isEmpty() || inputRef.startsWith(inputPrefix))
                            && (outputPrefix.isEmpty() || outputRef.startsWith(outputPrefix))
                            && outputRef.endsWith(outputSuffix);
                });
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

    @Test
    void testSelectedAssignmentKeepsDirectionAfterVectorAlias() {
        var code = """
                component top : [a] -> [out] {
                    wire a, t of bit(1);
                    wire x, y, out of bit(4);

                    t <> AND <> [a, a];
                    x[3] <> t;
                    y <> x;
                    out <> y;
                }
                """;
        var dump = assertDoesNotThrow(() -> NXBuilder.from(Configuration.asDefault())
                .load(code)
                .compileAndDump());
        var members = dump.get("design").get("members");

        assertTrue(hasToAlias(members, "t@", "", "[3]"));
        assertFalse(hasToAlias(members, "", "t@", "[3]"));
    }

    @Test
    void testSelectedAssignmentKeepsDirectionWithSimplifiedConsumer() {
        var code = """
                component top : [in_1] -> [out] {
                    wire in_1, tmp_214710, tmp_112690, out of bit(1);
                    wire _069645_ of bit(2);

                    tmp_112690 <> AND <> [1'b1, _069645_[1]];
                    tmp_214710 <> NOT <> [in_1];
                    _069645_[1] <> tmp_214710;
                    out <> tmp_112690;
                }
                """;
        var dump = assertDoesNotThrow(() -> NXBuilder.from(Configuration.asDefault())
                .load(code)
                .compileAndDump());
        var members = dump.get("design").get("members");

        assertTrue(hasToAlias(members, "tmp_214710@", "_069645_@", "[1]"));
        assertFalse(hasToAlias(members, "_069645_@", "tmp_214710@", ""));
    }

    private static String textOf(JsonNode node) {
        return node == null ? "" : node.asText();
    }
}
