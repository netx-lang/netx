package pascal.hardscript.compiler.elaborator;

import org.antlr.v4.runtime.CharStreams;
import org.junit.jupiter.api.Test;
import pascal.hardscript.compiler.ast.Identifier;
import pascal.hardscript.compiler.io.Frontend;
import pascal.hardscript.compiler.model.instance.DataAccess;
import pascal.hardscript.compiler.model.instance.Instance;
import pascal.hardscript.compiler.model.instance.Int32Value;
import pascal.hardscript.compiler.model.instance.selector.BitSelector;
import pascal.hardscript.compiler.model.scope.ComponentScope;
import pascal.hardscript.compiler.model.scope.DesignScope;
import pascal.hardscript.util.lang.BuiltinComponent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class EvaluatorUnitTest {

    private static DesignScope loadDesign(String code) {
        return new Elaborator(Frontend.load(CharStreams.fromString(code)).parse()).elaborate();
    }

    private static ComponentScope loadSingleComponent(String code) {
        return (ComponentScope) loadDesign(code).getChildren().getFirst();
    }

    @Test
    void testBuiltinFactoryIntegrity() {
        assertEquals(BuiltInComponentFactory.BUILTIN_FACTORY.size(), BuiltinComponent.values().length);
        for (var component : BuiltinComponent.values()) {
            assertTrue(BuiltInComponentFactory.BUILTIN_FACTORY.containsKey(component));
        }
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
                    [a[i] | i in [0..4]] -> [
                        gate | i in [0..2],
                               let gate = [NOT, NOT] <> AND <> NOT
                    ] <> AND -> [b]
                }
                """;
        assertDoesNotThrow(() -> loadSingleComponent(code));
    }
}