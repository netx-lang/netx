package pascal.netx.compiler.elaborator;

import org.antlr.v4.runtime.CharStreams;
import org.junit.jupiter.api.Test;
import pascal.netx.compiler.io.Configuration;
import pascal.netx.compiler.io.NXBuilder;
import pascal.netx.compiler.model.scope.ComponentScope;
import pascal.netx.compiler.model.scope.PackageScope;
import pascal.netx.compiler.model.variable.Int32Value;
import pascal.netx.compiler.model.variable.Variable;
import pascal.netx.compiler.model.variable.selector.BitSelector;
import pascal.netx.util.lang.Identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ComponentEvaluatorUnitTest {
    private static PackageScope loadDesign(String code) {
        return NXBuilder.from(Configuration.forExpandedTest())
                .load(CharStreams.fromString(code)).elaborate().getTop();
    }

    private static ComponentScope loadSingleComponent(String code) {
        return loadDesign(code).getTop();
    }

    @Test
    void testListComprehension() {
        var component = loadSingleComponent("""
                component inverter : [x[i] | i in [0..4]] -> [x[j] | i in [0..4], let j = 3 - i] {
                    wire x of bit(4);
                }
                """);
        var data = (Variable) component.getBinding().resolve(Identifier.of("x"));
        var expected = new ArrayList<>(IntStream.range(0, 4)
                .mapToObj(Int32Value::of).map(BitSelector::of)
                .map(selector -> Variable.accessOf(data, selector)).toList());
        assertEquals(expected, component.getInputs());
        Collections.reverse(expected);
        assertEquals(expected, component.getOutputs());
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
        assertDoesNotThrow(() -> loadSingleComponent(code));
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
        var cBuilder = NXBuilder.from(Configuration.forCompactTest());
        var eBuilder = NXBuilder.from(Configuration.forExpandedTest());
        assertEquals(
                cBuilder.load(expected).compileAndDump(),
                cBuilder.load(actual).compileAndDump()
        );
        assertEquals(
                eBuilder.load(expected).compileAndDump(),
                eBuilder.load(actual).compileAndDump()
        );
    }
}