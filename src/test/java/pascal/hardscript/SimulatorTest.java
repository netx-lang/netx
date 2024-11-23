package pascal.hardscript;

import org.junit.jupiter.api.Test;
import pascal.hardscript.compiler.io.Builder;
import pascal.hardscript.hson.simulator.Simulator;
import pascal.hardscript.hson.simulator.frontend.HFrontend;
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
        for (int i = 0; i <= 255; i++) {
            for (int j = 0; j <= 255; j++) {
                var expected = BitVector.fromInt(i + j, 8);
                simulator.setInput("a", BitVector.fromInt(i, 8));
                simulator.setInput("b", BitVector.fromInt(j, 8));
                simulator.run();
                assertEquals(expected, simulator.getOutput("c"));
            }
        }
    }

    @Test
    void testDecoder() throws IOException {
        var simulator = buildSimulator("decoder.hard");
        for (int i = 0; i < 8; i++) {
            var input = BitVector.fromInt(i, 3);
            var expected = BitVector.fromInt(1 << i, 8);
            simulator.setInput("input", input);
            simulator.run();
            assertEquals(expected, simulator.getOutput("output"));
        }
    }

    @Test
    void testEncoder() throws IOException {
        var simulator = buildSimulator("encoder.hard");
        for (int i = 0; i < 8; i++) {
            var input = BitVector.fromInt(1 << i, 8);
            var expected = BitVector.fromInt(i, 3);
            simulator.setInput("input", input);
            simulator.run();
            assertEquals(expected, simulator.getOutput("output"));
        }
    }

    @Test
    void testDivideBy3() throws IOException {
        var simulator = buildSimulator("divide_by_3.hard");
        List<Integer> clock = new LinkedList<>();
        simulator.setOnEachTick(env -> clock.add(env.getOutput("clk_3").toInt()));
        simulator.runUntil(20);
        assertEquals(List.of(0, 0, 0, 1, 1, 1, 0, 0, 0, 1, 1, 1, 0, 0, 0, 1, 1, 1, 0, 0), clock);
    }

    @Test
    void testCounter() throws IOException {
        var simulator = buildSimulator("counter.hard");
        var high = BitVector.fromBoolean(true);
        var low = BitVector.fromBoolean(false);

        List<Integer> count = new LinkedList<>();
        simulator.setOnEachTick(env -> count.add(env.getOutput("count").toInt()));
        simulator.setInput("rst", high);
        simulator.runUntil(1);
        simulator.setInput("rst", low);
        simulator.runUntil(6);
        simulator.setInput("rst", high);
        simulator.runUntil(3);
        assertEquals(List.of(0, 0, 0, 1, 1, 2, 2, 3, 3, 0), count);
    }

    private static Simulator buildSimulator(String other) throws IOException {
        var design = Builder.forExpanded().loadPath(TEST_DIR.resolve(other)).compile();
        var circuit = HFrontend.load(design).parse();
        return new Simulator(circuit);
    }
}
