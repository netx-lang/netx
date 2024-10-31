package pascal.hardscript.compiler.elaborator;

import org.antlr.v4.runtime.CharStreams;
import org.junit.jupiter.api.Test;
import pascal.hardscript.compiler.ast.Identifier;
import pascal.hardscript.compiler.io.Frontend;
import pascal.hardscript.compiler.model.components.Component;
import pascal.hardscript.compiler.model.components.ComponentAccess;
import pascal.hardscript.compiler.model.components.selector.BitSelector;
import pascal.hardscript.compiler.model.scope.DesignScope;
import pascal.hardscript.compiler.model.scope.ModuleScope;
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

    private static ModuleScope loadSingleModule(String code) {
        return (ModuleScope) loadDesign(code).getChildren().getFirst();
    }

    @Test
    void testListComprehension() {
        var module = loadSingleModule("""
                module inverter : [x[i] | i in [0..4]] -> [x[j] | i in [0..4], let j = 3 - i] {
                    inst x of wire(bit(4))
                }
                """);
        var component = (Component) module.getBinding().resolve(Identifier.of("x"));
        var expected = new ArrayList<>(IntStream.range(0, 4)
                .mapToObj(Int32Value::of).map(BitSelector::of)
                .map(selector -> ComponentAccess.of(component, selector)).toList());
        assertEquals(expected, module.getInputs());
        Collections.reverse(expected);
        assertEquals(expected, module.getOutputs());
    }

    @Test
    void testInstanceType() {
        var code = """
                module test : [a] -> [b] {
                    inst a of wire(bit(4))
                    inst b of wire(bit(1))
                    [gate |
                        i in [0..2],
                        let gate = [NOT, NOT] <> AND <> NOT
                    ] <> AND : [a[i] | i in [0..4]] -> [b]
                }
                """;
        assertDoesNotThrow(() -> loadSingleModule(code));
    }
}