package pascal.hardscript;

import org.junit.jupiter.api.Test;
import pascal.hardscript.compiler.io.HSCompiler;
import pascal.hardscript.compiler.io.HSPrinter;
import pascal.hardscript.hson.io.HFrontend;
import pascal.hardscript.hson.simulator.Simulator;
import pascal.hardscript.util.lang.BitVector;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SimulatorTest {
    private static final Path TEST_DIR = Path.of("src/test/resources/");

    @Test
    void testAdder() throws IOException {
        var simulator = buildSimulator("adder.hard");
        var high = BitVector.fromBoolean(true);
        var low = BitVector.fromBoolean(false);
        simulator.setInputValue("a", high);
        simulator.setInputValue("b", high);
        simulator.setInputValue("cin", high);
        simulator.run();
        assertEquals(high, simulator.getOutputValue("out"));
        assertEquals(high, simulator.getOutputValue("cout"));
        simulator.setInputValue("a", low);
        simulator.run();
        assertEquals(low, simulator.getOutputValue("out"));
        assertEquals(high, simulator.getOutputValue("cout"));
    }

    @Test
    void testDecoder() throws IOException {
        var simulator = buildSimulator("decoder.hard");
        for (int i = 0; i < 8; i++) {
            var input = BitVector.fromInt(i, 3);
            var expected = BitVector.fromInt(1 << i, 8);
            simulator.setInputValue("input", input);
            simulator.run();
            assertEquals(expected, simulator.getOutputValue("output"));
        }
    }

    @Test
    void testDivideBy3() throws IOException {
        var simulator = buildSimulator("divide_by_3.hard");
        List<Integer> clock = new LinkedList<>();
        simulator.setOnEachTick(env -> clock.add(env.getOutputValue("clk_3").toInt()));
        simulator.runUntil(20);
        assertEquals(List.of(0, 0, 0, 1, 1, 1, 0, 0, 0, 1, 1, 1, 0, 0, 0, 1, 1, 1, 0, 0), clock);
    }

    private static Simulator buildSimulator(String other) throws IOException {
        var design = HSCompiler.loadPath(TEST_DIR.resolve(other)).compile();
        var circuit = HFrontend.load(new HSPrinter(design).dump()).parse();
        return new Simulator(circuit);
    }
}
