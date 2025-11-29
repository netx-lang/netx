package pascal.netx.compiler.elaborator;

import org.junit.jupiter.api.Test;
import pascal.netx.compiler.circuit.signal.Signal;
import pascal.netx.compiler.circuit.signal.selector.BitSelector;
import pascal.netx.compiler.io.Configuration;
import pascal.netx.compiler.io.NXBuilder;
import pascal.netx.compiler.utils.CompileCancellationException;
import pascal.netx.util.lang.Identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static pascal.netx.test.TestUtils.*;

class ElaboratorUnitTest {
    @Test
    void testEnum() {
        testGen("enum");
    }

    @Test
    void testFrege() {
        testGen("frege");
    }

    @Test
    void testBind() {
        testGen("bind_1");
        testGen("bind_2");
        testGen("bind_3");
    }

    @Test
    void testAttr() {
        testGen("attr_1");
        testGen("attr_2");
        testGen("attr_3");
    }

    @Test
    void testListComprehension() {
        var component = testSingleComponent("""
                component inverter : [x[i] | i in [0..4]] -> [x[j] | i in [0..4], let j = 3 - i] {
                    wire x of bit(4);
                }
                """);
        var data = (Signal) component.getBinding().resolve(Identifier.of("x"));
        var expected = new ArrayList<>(IntStream.range(0, 4)
                .mapToObj(BitSelector::new)
                .map(selector -> Signal.accessOf(data, selector)).toList());
        assertEquals(expected, component.getInputs());
        Collections.reverse(expected);
        assertEquals(expected, component.getOutputs());
    }

    @Test
    void testParallelExpr() {
        var expected = """
                component top : [a, b] -> [c] {
                    wire a, b, c of bit(4);
                    c <> AND <> [a, b];
                }
                """;
        var actual = """
                component top : [a, b] -> [c] {
                    wire a, b, c of bit(4);
                    c <> AND <> (a || b);
                }
                """;
        testSugar(expected, actual);
    }

    @Test
    void testInstantiation() {
        var code = """
                component top : [a] -> [b] {
                    wire a of bit(4);
                    wire b of bit(1);
                    b <> AND <> [gate |
                        i in [0..2],
                        let gate = NOT <> AND <> [NOT, NOT]
                    ] <> [a[i] | i in [0..4]];
                }
                """;
        assertDoesNotThrow(() -> testSingleComponent(code));
    }

    @Test
    void testNamedCall() {
        var expected = """
                component parametric_comp(a, b, c) : i -> o {
                    auto i, o;
                    o <> AND <> [ AND <> [ AND <> [i, c], b], a];
                }
                component top : i -> o {
                    wire i, o of bit(32);
                    let parametric_marco(a, b) = parametric_comp(a, b, 1);
                    o <> parametric_marco(1, 2) <>
                         parametric_comp(3, 4, 5) <> i;
                }
                """;
        var actual = """
                component parametric_comp(a, b, c) : i -> o {
                    auto i, o;
                    o <> AND <> [ AND <> [ AND <> [i, c], b], a];
                }
                component top : i -> o {
                    wire i, o of bit(32);
                    let parametric_marco(a, b) = parametric_comp(c=1, a=a, b=b);
                    o <> parametric_marco(b =2, a=1) <>
                         parametric_comp(b=4, a=3, c = 5) <> i;
                }
                """;
        testSugar(expected, actual);
    }

    @Test
    void testConstant() {
        var code = """
                component top : [] -> o {
                    auto o <> NOT <> 1;
                }
                component infer : i -> o {
                    auto o <> NOT <> i;
                }
                """;
        assertDoesNotThrow(() -> testSingleComponent(code));
    }

    @Test
    void testBranch() {
        var code = """
                component top(cond) : i -> o {
                    auto i, o;
                    if (cond)
                    then o <> AND <> [i, 1];
                    else o <> OR <> [i, 1];
                }
                """;
        var thenConf = Configuration.builder().withArgs(List.of("cond=true")).build();
        var elseConf = Configuration.builder().withArgs(List.of("cond=false")).build();
        assertNotEquals(
                NXBuilder.from(thenConf).load(code).compileAndDump(),
                NXBuilder.from(elseConf).load(code).compileAndDump()
        );
    }

    @Test
    void testInferArg() {
        var actual = """
                component top : [a, b, c, d] -> [e] {
                    wire a, b, c, d, e of bit(4);
                    e <> AND <> [a, b, c, d];
                }
                """;
        var expected = """
                component top : [a, b, c, d] -> [e] {
                    wire a, b, c, d, e of bit(4);
                    e <> AND(4) <> [a, b, c, d];
                }
                """;
        testSugar(expected, actual);
    }

    @Test
    void testNotInferArg() {
        var actual = """
                component top : [a, b, c, d] -> [e] {
                    wire a, b, c, d, e of bit(4);
                    e <> AND <> [AND, AND] <> [a, b, c, d];
                }
                """;
        var expected = """
                component top : [a, b, c, d] -> [e] {
                    wire a, b, c, d, e of bit(4);
                    e <> AND <> [
                      AND <> [a, b],
                      AND <> [c, d]
                    ];
                }
                """;
        testSugar(expected, actual);
    }

    @Test
    void testNamedPortConnection() {
        var actual = """
                component top : [] -> [o] {
                    auto o <> sub <> { c : 3, a : 1, d : 4, e : 5, b : 2 };
                }
                component sub : [a, b, c, d, e] -> [f] {
                    wire a, b, c, d, e, f of bit(32);
                    f <> AND <> [a, b, c, d, e];
                }
                """;
        var expected = """
                component top : [] -> [o] {
                    auto o <> sub <> [1, 2, 3, 4, 5];
                }
                component sub : [a, b, c, d, e] -> [f] {
                    wire a, b, c, d, e, f of bit(32);
                    f <> AND <> [a, b, c, d, e];
                }
                """;
        testSugar(expected, actual);
    }

    @Test
    void testAccessPortConnection() {
        var actual = """
                component top : [] -> [o] {
                    auto o <> sub <> { a[0]: 1'b1, b: 4'b1101, a[1]: 1'b0, a[2]: 1'b1, a[3]: 1'b0 };
                }
                component sub : [b] :: [a[i] | i in [0..4]] -> [c] {
                    wire a, b, c of bit(4);
                    c <> AND <> [a, b];
                }
                """;
        var expected = """
                component top : [] -> [o] {
                    auto o <> sub <> [4'b1101, 1'b1, 1'b0, 1'b1, 1'b0];
                }
                component sub : [b] :: [a[i] | i in [0..4]] -> [c] {
                    wire a, b, c of bit(4);
                    c <> AND <> [a, b];
                }
                """;
        testSugar(expected, actual);
    }

    @Test
    void testClosedMacro() {
        var good = """
                component top : [] -> [] {
                    let f(a) = AND <> [1'b1, a];
                    wire a of bit(1);
                    f(a);
                }
                """;
        var bad = """
                component top : [] -> [] {
                    let f = AND <> [1'b1, a];
                    wire a of bit(1);
                    f;
                }
                """;
        assertDoesNotThrow(() -> testSingleComponent(good));
        assertThrows(CompileCancellationException.class, () -> testSingleComponent(bad));
    }
}