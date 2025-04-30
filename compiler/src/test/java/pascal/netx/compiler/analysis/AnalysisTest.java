package pascal.netx.compiler.analysis;

import org.antlr.v4.runtime.CharStreams;
import org.junit.jupiter.api.Test;
import pascal.netx.compiler.exception.TypeCheckingException;
import pascal.netx.compiler.io.Configuration;
import pascal.netx.compiler.io.NXBuilder;
import pascal.netx.compiler.io.NXParser;
import pascal.netx.util.TestUtils;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;


class AnalysisTest {
    private static final Path TEST_DIR = Path.of("src/test/resources/testcases");

    @Test
    void testSourceLocChecker() throws IOException {
        for (var testcase : TestUtils.collectAllFilesUnder(TEST_DIR)) {
            if (testcase.getFileName().endsWith(".nx")) {
                var node = new NXParser(CharStreams.fromPath(testcase)).parse();
                new SourceLocChecker(node).analyze();
            }
        }
    }

    @Test
    void testDataTypeInference() {
        var expected = """
                component top {
                    wire clk of clock();
                    wire a of bit(4);
                    wire b of bit(4);
                    wire x of bit(4);
                    wire c of bit(2);
                    x <> OR <> [a, b];
                    c <> NOT <> REGISTER(true) <> [AND <> [a[1:0], b[1:0]], clk];
                }
                """;
        var actual = """
                component top {
                    wire clk of clock();
                    wire a of bit(4);
                    wire b of bit(4);
                    auto x;
                    auto c;
                    x <> OR <> [a, b];
                    c <> NOT <> REGISTER(true) <> [AND <> [a[1:0], b[1:0]], clk];
                }
                """;
        var builder = NXBuilder.from(Configuration.forExpandedTest());
        assertEquals(
                builder.load(expected).compileAndDump(),
                builder.load(actual).compileAndDump()
        );
    }

    @Test
    void testNestedDataTypeInference() {
        var code = """
                component top {
                    wire a of bit(3);
                    auto b <> sub <> a;
                }
                component sub: [a] -> [b] {
                    auto a, b;
                    b <> tricky <> [a, CAST <> 1'b0];
                }
                component tricky : [a, b] -> [c] {
                    auto a, b;
                    auto c <> AND <> [
                        AND <> [a, CAST <> 1'b1],
                        AND <> [b, CAST <> 1'b1]
                    ];
                }
                """;
        assertDoesNotThrow(() -> NXBuilder.from(Configuration.forExpandedTest()).load(code).compileAndDump());
    }

    @Test
    void testDuplicatedElimination() {
        var expected = """
                component top: [a, b] -> [e] {
                    wire a, b, c, e of bit(4);
                    c <> AND <> [a, b];
                    e <> ADD <> [c, c];
                }
                """;
        var actual = """
                component top: [a, b] -> [e] {
                    wire a, b, c, e, d of bit(4);
                    c <> AND <> [a, b];
                    e <> ADD <> [c, d];
                    d <> AND <> [a, b];
                }
                """;
        var builder = NXBuilder.from(Configuration.forExpandedTest());
        assertEquals(
                builder.load(expected).compileAndDump(),
                builder.load(actual).compileAndDump());
    }

    @Test
    void testSelector() {
        var expected = """
                component top: [a] -> [b, c, d, e, f] {
                    wire a of bit(8);
                    auto b, c, d, e, f;
                    b <> a[5:3];
                    c <> a[1];
                    d <> a[3];
                    e <> 7'b1001011;
                    f <> a;
                }
                """;
        var actual = """
                component top: [a] -> [b, c, d, e, f] {
                    wire a of bit(8);
                    auto b, c, d, e, f;
                    b <> a[5:2][3:1];
                    c <> a[1][0];
                    d <> a[4:3][0];
                    e <> 8'b10010110[7:1];
                    f <> a;
                }
                """;
        var builder = NXBuilder.from(Configuration.forExpandedTest());
        assertEquals(
                builder.load(expected).compileAndDump(),
                builder.load(actual).compileAndDump());
    }

    @Test
    void testInvalidAccess() {
        var a = """
                component top {
                    wire a, b of bit(4);
                    a[1:0] <> b[5:4];
                }
                """;
        var b = """
                component top {
                    wire a, b of bit(4);
                    a[1:0] <> AND <> [b[5:4], b[3:2]];
                }
                """;
        var builder = NXBuilder.from(Configuration.forExpandedTest());
        assertThrows(TypeCheckingException.class, () -> builder.load(a).compileAndDump());
        assertThrows(TypeCheckingException.class, () -> builder.load(b).compileAndDump());
    }
}