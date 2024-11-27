package pascal.hardscript.compiler.elaborator;

import org.antlr.v4.runtime.CharStreams;
import org.junit.jupiter.api.Test;
import pascal.hardscript.compiler.io.Builder;
import pascal.hardscript.compiler.model.instance.Instance;
import pascal.hardscript.compiler.model.instance.Int32Value;
import pascal.hardscript.compiler.model.instance.selector.BitSelector;
import pascal.hardscript.compiler.model.scope.ComponentScope;
import pascal.hardscript.compiler.model.scope.GlobalScope;
import pascal.hardscript.util.lang.Identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class EvaluatorUnitTest {

    private static GlobalScope loadDesign(String code) {
        return Builder.forExpanded().load(CharStreams.fromString(code)).elaborate();
    }

    private static ComponentScope loadSingleComponent(String code) {
        return loadDesign(code).getTop();
    }

    @Test
    void testListComprehension() {
        var component = loadSingleComponent("""
                component inverter : [x[i] | i in [0..4]] -> [x[j] | i in [0..4], let j = 3 - i] {
                    inst x of wire(bit(4))
                }
                """);
        var data = (Instance) component.getBinding().resolve(Identifier.of("x"));
        var expected = new ArrayList<>(IntStream.range(0, 4)
                .mapToObj(Int32Value::of).map(BitSelector::of)
                .map(selector -> Instance.accessOf(data, selector)).toList());
        assertEquals(expected, component.getInputs());
        Collections.reverse(expected);
        assertEquals(expected, component.getOutputs());
    }

    @Test
    void testInstanceType() {
        var code = """
                component test : [a] -> [b] {
                    inst a of wire(bit(4))
                    inst b of wire(bit(1))
                    b <> AND <> [gate |
                        i in [0..2],
                        let gate = NOT <> AND <> [NOT, NOT]
                    ] <> [a[i] | i in [0..4]]
                }
                """;
        assertDoesNotThrow(() -> loadSingleComponent(code));
    }
}