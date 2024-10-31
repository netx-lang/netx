package pascal.hardscript.compiler.elaborator;

import org.antlr.v4.runtime.CharStreams;
import org.junit.jupiter.api.Test;
import pascal.hardscript.compiler.ast.Identifier;
import pascal.hardscript.compiler.io.Frontend;
import pascal.hardscript.compiler.model.data.Data;
import pascal.hardscript.compiler.model.data.DataAccess;
import pascal.hardscript.compiler.model.data.selector.BitSelector;
import pascal.hardscript.compiler.model.scope.ComponentScope;
import pascal.hardscript.compiler.model.scope.DesignScope;
import pascal.hardscript.compiler.model.value.Int32Value;

import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class EvaluatorUnitTest {

    private static DesignScope loadDesign(String code) {
        return Frontend.load(CharStreams.fromString(code)).elaborate();
    }

    private static ComponentScope loadSingleComponent(String code) {
        return (ComponentScope) loadDesign(code).getChildren().getFirst();
    }

    @Test
    void testListComprehension() {
        var component = loadSingleComponent("""
                component inverter : [x[i] | i in [0..4]] -> [x[j] | i in [0..4], let j = 3 - i] {
                    inst x of wire(bit(4))
                }
                """);
        var data = (Data) component.getBinding().resolve(Identifier.of("x"));
        var expected = new ArrayList<>(IntStream.range(0, 4)
                .mapToObj(Int32Value::of).map(BitSelector::of)
                .map(selector -> DataAccess.of(data, selector)).toList());
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
                    [gate |
                        i in [0..2],
                        let gate = [NOT, NOT] <> AND <> NOT
                    ] <> AND : [a[i] | i in [0..4]] -> [b]
                }
                """;
        assertDoesNotThrow(() -> loadSingleComponent(code));
    }
}