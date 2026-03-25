package pascal.netx.compiler.analysis;

import org.antlr.v4.runtime.CharStreams;
import org.junit.jupiter.api.Test;
import pascal.netx.compiler.NXCompiler;
import pascal.netx.compiler.backend.analysis.ast.SourceLocChecker;
import pascal.netx.compiler.backend.analysis.circuit.constraint.EqualTypeConstraint;
import pascal.netx.compiler.io.Configuration;
import pascal.netx.compiler.io.NXBuilder;
import pascal.netx.compiler.io.NXParser;
import pascal.netx.compiler.model.circuit.scope.ComponentScope;
import pascal.netx.compiler.model.circuit.scope.PackageScope;
import pascal.netx.compiler.model.circuit.scope.RepoScope;
import pascal.netx.compiler.model.circuit.signal.Signal;
import pascal.netx.compiler.model.circuit.signal.Wire;
import pascal.netx.compiler.model.circuit.signal.selector.BitSelector;
import pascal.netx.compiler.model.type.BitType;
import pascal.netx.compiler.utils.CompileCancellationException;
import pascal.netx.compiler.utils.TypeCheckingException;
import pascal.netx.util.lang.Identifier;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static pascal.netx.test.TestUtils.*;


class AnalysisTest {
    @Test
    void testSourceLocChecker() throws IOException {
        for (var testcase : collectAllFilesUnder(TEST_DIR)) {
            if (testcase.getFileName().endsWith(".nx")) {
                var node = new NXParser(CharStreams.fromPath(testcase), "test").parse();
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
    void testDirectAliasA() {
        var code = """
                component top : i -> o {
                  wire i of bit(1);
                  wire o of bit(5);
                  for index in [0..5] {
                    o[index] <> i;
                  }
                }
                """;
        testGen(EXPECTED_DIR.resolve("direct_alias_a.json"), code);
    }

    @Test
    void testDirectAliasB() {
        var code = """
                component top : i -> o {
                  wire i of bit(5);
                  wire o of bit(5);
                  for index in [0..5] {
                    o[4 - index] <> i[index];
                  }
                }
                """;
        testGen(EXPECTED_DIR.resolve("direct_alias_b.json"), code);
    }

    @Test
    void testDirectAliasC() {
        var code = """
                component top : i -> o {
                  wire i of bit(5);
                  wire o of bit(1);
                  o <> i[4];
                }
                """;
        testGen(EXPECTED_DIR.resolve("direct_alias_c.json"), code);
    }

    @Test
    void testDirectAliasD() {
        var code = """
                component top : a -> [b, c] {
                  wire a, b of bit(5);
                  wire c of bit(2);
                  c[0] <> b[0];
                  c[1] <> b[1];
                  for index in [0..5] {
                    b[index] <> a[index];
                  }
                }
                """;
        testGen(EXPECTED_DIR.resolve("direct_alias_d.json"), code);
    }

    @Test
    void testDirectAliasE() {
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
        testGen(EXPECTED_DIR.resolve("direct_alias_e.json"), code);
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
        testSugar(expected, code);
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
        testSugar(expected, code);
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
        testSugar(expected, code);
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
        testSugar(expected, code);
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
        testSugar(expected, code);
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
        testSugar(expected, code);
    }

    @Test
    void testDup() {
        var code = """
                component top : a -> b {
                    wire a    of bit(4);
                    wire b[4] of bit(4);
                    [b[i] | i in [0..4]] <> DUP(4) <> a;
                }
                """;
        var expected = """
                component top : a -> b {
                    wire a    of bit(4);
                    wire b[4] of bit(4);
                    b[0] <> a;
                    b[1] <> a;
                    b[2] <> a;
                    b[3] <> a;
                }
                """;
        testSugar(expected, code);
    }

    @Test
    void testSplit() {
        var code = """
                component top : a -> b {
                    wire a, b of bit(4);
                    [b[i] | i in [0..4]] <> SPLIT(4) <> a;
                }
                """;
        var expected = """
                component top : a -> b {
                    wire a, b of bit(4);
                    b[0] <> a[3];
                    b[1] <> a[2];
                    b[2] <> a[1];
                    b[3] <> a[0];
                }
                """;
        testSugar(expected, code);
    }

    @Test
    void testCombinationalLoop() {
        var code = """
                component top {
                    wire a, b, c, d of bit(1);
                    a <> AND <> [b, c];
                    d <> OR <> [a, b];
                    c <> NOT <> d;
                }
                """;
        assertThrows(CompileCancellationException.class, () -> compileSingleComponent(code));
    }

    @Test
    void testComplexAccessAlias() {
        var code = """
                component top: [x, y] -> [z, w, v, u] {
                    wire x, y, z, w of bit(1);
                    wire a, v, u of bit(4);
                
                    a[0] <> NOT <> x;
                    a[1] <> NOT <> y;
                    a[2] <> ADD <> [a[0], a[1]];
                    a[3] <> NOT <> a[2];
                    z <> NOT <> a[2];
                    w <> NOT <> a[3];
                    for i in [0..4] {
                        v[i] <> a[3 - i];
                    }
                    u <> ADD <> [a, v];
                }
                """;
        testGen(EXPECTED_DIR.resolve("complex_access_alias.json"), code);
    }

    @Test
    void testTruncate() {
        var code = """
                component top: [clk, input] -> [output] {
                    wire clk of clock;
                    wire input of bit(4);
                    wire output of bit(8);
                    output <> REGISTER(pos_edge=true) <> [
                      clk, CAST <> AND <> [input, CAST <> output]
                    ];
                }
                """;
        var expected = """
                component top: [clk, input] -> [output] {
                    wire clk of clock;
                    wire input of bit(4);
                    wire output of bit(8);
                    output <> REGISTER(pos_edge=true) <> [
                      clk, CAST <> AND <> [input, output[3:0]]
                    ];
                }
                """;
        testSugar(expected, code);
    }

    @Test
    void testEqualTypeConstraintErrorFormatsScopeChainOnSeparateLines() {
        var repo = new RepoScope();
        var pack = PackageScope.newPackage(repo);
        var lhsTop = ComponentScope.newComponent(pack, Identifier.of("lhs_top"));
        var lhsParent = ComponentScope.newComponent(lhsTop, Identifier.of("lhs_parent"));
        var rhsTop = ComponentScope.newComponent(pack, Identifier.of("rhs_top"));
        var rhsMid = ComponentScope.newComponent(rhsTop, Identifier.of("rhs_mid"));
        var rhsParent = ComponentScope.newComponent(rhsMid, Identifier.of("rhs_parent"));
        var indexer = new NXCompiler.WireIndexer(new AtomicInteger()::incrementAndGet);
        var factory = new Wire.Factory(BitType.of(1), indexer);
        var lhs = factory.create(lhsParent, Identifier.of("lhs"));
        var rhs = Signal.accessOf(factory.create(rhsParent, Identifier.of("rhs")), new BitSelector(0));

        var error = new EqualTypeConstraint(lhs, rhs).error();

        assertEquals("""
                Type mismatch:
                  lhs: lhs@()
                      in scope lhs_top@()
                      in scope lhs_parent@()
                  rhs: rhs@()[0]
                      in scope rhs_top@()
                      in scope rhs_mid@()
                      in scope rhs_parent@()""", error);
    }
}