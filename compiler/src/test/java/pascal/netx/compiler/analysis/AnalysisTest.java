package pascal.netx.compiler.analysis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.antlr.v4.runtime.CharStreams;
import org.junit.jupiter.api.Test;
import pascal.netx.compiler.analysis.ast.SourceLocChecker;
import pascal.netx.compiler.exception.TypeCheckingException;
import pascal.netx.compiler.io.Configuration;
import pascal.netx.compiler.io.NXBuilder;
import pascal.netx.compiler.io.NXParser;
import pascal.netx.util.TestUtils;
import pascal.netx.util.print.NotepadWriter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static pascal.netx.compiler.TestUtils.test;


class AnalysisTest {
    private static final Path TEST_DIR = Path.of("src/test/resources/testcases");
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final boolean GENERATE_EXPECTED = false;

    @Test
    void testSourceLocChecker() throws IOException {
        for (var testcase : TestUtils.collectAllFilesUnder(TEST_DIR)) {
            if (testcase.getFileName().endsWith(".nx")) {
                var node = new NXParser(CharStreams.fromPath(testcase), "test").parse();
                new SourceLocChecker(node).analyze();
            }
        }
    }

    private static void testEquals(String code, String output) throws IOException {
        Configuration config = Configuration.asDefault();
        if (GENERATE_EXPECTED) {
            var builder = NXBuilder.from(config);
            var compiler = builder.load(code);
            builder.printer(NotepadWriter.pathOut(TEST_DIR.resolve(output))).print(compiler.compileAndDump());
        } else {
            var compiler = NXBuilder.from(config).load(code);
            var expected = MAPPER.reader().readTree(Files.readString(TEST_DIR.resolve(output)));
            assertEquals(expected, compiler.compileAndDump());
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
                    c <> NOT <> REGISTER(true) <> [clk, AND <> [a[1:0], b[1:0]]];
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
                    c <> NOT <> REGISTER(true) <> [clk, AND <> [a[1:0], b[1:0]]];
                }
                """;
        var builder = NXBuilder.from(Configuration.asDefault());
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
        assertDoesNotThrow(() -> NXBuilder.from(Configuration.asDefault()).load(code).compileAndDump());
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
        var builder = NXBuilder.from(Configuration.asDefault());
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
        var builder = NXBuilder.from(Configuration.asDefault());
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
        var builder = NXBuilder.from(Configuration.asDefault());
        assertThrows(TypeCheckingException.class, () -> builder.load(a).compileAndDump());
        assertThrows(TypeCheckingException.class, () -> builder.load(b).compileAndDump());
    }

    @Test
    void testDirectAliasA() throws IOException {
        var code = """
                component top : i -> o {
                  wire i of bit(1);
                  wire o of bit(5);
                  for (index in [0..5]) {
                    o[index] <> i;
                  }
                }
                """;
        testEquals(code, "expected/direct_alias_a.json");
    }

    @Test
    void testDirectAliasB() throws IOException {
        var code = """
                component top : i -> o {
                  wire i of bit(5);
                  wire o of bit(5);
                  for (index in [0..5]) {
                    o[4 - index] <> i[index];
                  }
                }
                """;
        testEquals(code, "expected/direct_alias_b.json");
    }

    @Test
    void testDirectAliasC() throws IOException {
        var code = """
                component top : i -> o {
                  wire i of bit(5);
                  wire o of bit(1);
                  o <> i[4];
                }
                """;
        testEquals(code, "expected/direct_alias_c.json");
    }

    @Test
    void testDirectAliasD() throws IOException {
        var code = """
                component top : a -> [b, c] {
                  wire a, b of bit(5);
                  wire c of bit(2);
                  c[0] <> b[0];
                  c[1] <> b[1];
                  for (index in [0..5]) {
                    b[index] <> a[index];
                  }
                }
                """;
        testEquals(code, "expected/direct_alias_d.json");
    }

    @Test
    void testDirectAliasE() throws IOException {
        var code = """
                component top : a -> [b, c] {
                  wire a, b of bit(2);
                  wire c of bit(1);
                  wire tmp of bit(5);
                  tmp[1:0] <> a;
                  tmp[3] <> 1'b1;
                  tmp[2] <> XOR <> [a[0], a[1]];
                  tmp[4] <> AND <> [a[0], a[1]];
                  b[0] <> tmp[4];
                  b[1] <> tmp[2];
                  c <> tmp[3];
                }
                """;
        testEquals(code, "expected/direct_alias_e.json");
    }

    @Test
    void testConstantGateAnd() {
        var code = """
                component top : a -> b {
                    wire a, b of bit(1);
                    b <> AND <> [a, 1'b1];
                }
                """;
        var expected = """
                component top : a -> b {
                    wire a, b of bit(1);
                    b <> a;
                }
                """;
        test(expected, code);
    }

    @Test
    void testConstantGateOr() {
        var code = """
                component top : a -> b {
                    wire a, b of bit(1);
                    b <> OR <> [a, 1'b0];
                }
                """;
        var expected = """
                component top : a -> b {
                    wire a, b of bit(1);
                    b <> a;
                }
                """;
        test(expected, code);
    }

    @Test
    void testConstantGateXor1() {
        var code = """
                component top : a -> b {
                    wire a, b of bit(1);
                    b <> XOR <> [a, 1'b0];
                }
                """;
        var expected = """
                component top : a -> b {
                    wire a, b of bit(1);
                    b <> a;
                }
                """;
        test(expected, code);
    }

    @Test
    void testConstantGateXor2() {
        var code = """
                component top : a -> b {
                    wire a, b of bit(1);
                    b <> XOR <> [a, 1'b1];
                }
                """;
        var expected = """
                component top : a -> b {
                    wire a, b of bit(1);
                    b <> NOT <> a;
                }
                """;
        test(expected, code);
    }

    @Test
    void testConstantGateNand() {
        var code = """
                component top : a -> b {
                    wire a, b of bit(1);
                    b <> NAND <> [a, 1'b1];
                }
                """;
        var expected = """
                component top : a -> b {
                    wire a, b of bit(1);
                    b <> NOT <> a;
                }
                """;
        test(expected, code);
    }

    @Test
    void testConstantGateNor() {
        var code = """
                component top : a -> b {
                    wire a, b of bit(1);
                    b <> NOR <> [a, 1'b0];
                }
                """;
        var expected = """
                component top : a -> b {
                    wire a, b of bit(1);
                    b <> NOT <> a;
                }
                """;
        test(expected, code);
    }
}