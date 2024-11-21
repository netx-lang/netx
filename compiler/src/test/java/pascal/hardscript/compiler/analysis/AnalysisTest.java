package pascal.hardscript.compiler.analysis;

import org.junit.jupiter.api.Test;
import pascal.hardscript.compiler.io.Builder;
import pascal.hardscript.util.TestUtils;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;


class AnalysisTest {

    private static final Path TEST_DIR = Path.of("src/test/resources/");

    @Test
    void testSourceLocChecker() throws IOException {
        for (var testcase : TestUtils.collectAllFilesUnder(TEST_DIR)) {
            var frontend = Builder.forExpanded().loadPath(testcase);
            new SourceLocChecker(frontend.parse()).analyze();
        }
    }

    @Test
    void testDataTypeInference() {
        var expected = """
                component top {
                    inst clk of clock()
                    inst a of wire(bit(4))
                    inst b of wire(bit(4))
                    inst x of wire(bit(4))
                    inst c of wire(bit(2))
                    [x] <- OR <> [a, b]
                    [c] <- NOT <> REGISTER(clk, true) <> AND <> [a[1:0], b[1:0]]
                }
                """;
        var actual = """
                component top {
                    inst clk of clock()
                    inst a of wire(bit(4))
                    inst b
                    inst x
                    inst c
                    [x] <- OR <> [a, b]
                    [c] <- NOT <> REGISTER(clk, true) <> AND <> [a[1:0], b[1:0]]
                }
                """;
        assertEquals(
                Builder.forExpanded().load(expected).compile(),
                Builder.forExpanded().load(actual).compile());
    }

    @Test
    void testNestedDataTypeInference() {
        var file = TEST_DIR.resolve("nested_inference.hard");
        assertDoesNotThrow(() -> Builder.forExpanded().loadPath(file).compile());
    }

    @Test
    void testDuplicatedElimination() {
        var expected = """
                component top: [a, b] -> [e] {
                    inst a, b, c, e of wire(bit(4))
                    c <- AND <> [a, b]
                    e <- ADD <> [c, c]
                }
                """;
        var actual = """
                component top: [a, b] -> [e] {
                    inst a, b, c, e, d of wire(bit(4))
                    c <- AND <> [a, b]
                    e <- ADD <> [c, d]
                    d <- AND <> [a, b]
                }
                """;
        assertEquals(
                Builder.forExpanded().load(expected).compile(),
                Builder.forExpanded().load(actual).compile());
    }

    @Test
    void testSelector() {
        var expected = """
                component top: [a] -> [b, c, d, e] {
                    inst a of wire(bit(8))
                    inst b, c, d, e
                    b <- a[5:3]
                    c <- a[1]
                    d <- a[3]
                    e <- 7'b1001011
                }
                """;
        var actual = """
                component top: [a] -> [b, c, d, e] {
                    inst a of wire(bit(8))
                    inst b, c, d, e
                    b <- a[5:2][3:1]
                    c <- a[1][0]
                    d <- a[4:3][0]
                    e <- 8'b10010110[7:1]
                }
                """;
        assertEquals(
                Builder.forExpanded().load(expected).compile(),
                Builder.forExpanded().load(actual).compile());
    }
}