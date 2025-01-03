package pascal.netx;

import org.junit.jupiter.api.Test;
import pascal.netx.compiler.io.Builder;
import pascal.netx.simulator.Simulator;
import pascal.netx.simulator.frontend.HFrontend;
import pascal.netx.util.lang.BitVector;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SimulatorTest {
    private static final Path TEST_DIR = Path.of("src/test/resources/testcases/");

    @Test
    void testAdder() throws IOException {
        var simulator = buildSimulator("adder.nx");
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
        var simulator = buildSimulator("decoder.nx");
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
        var simulator = buildSimulator("encoder.nx");
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
        var simulator = buildSimulator("divide_by_3.nx");
        List<Integer> clock = new LinkedList<>();
        simulator.setOnEachTick(env -> clock.add(env.getOutput("clk_3").toInt()));
        simulator.runUntil(20);
        assertEquals(List.of(0, 0, 0, 1, 1, 1, 0, 0, 0, 1, 1, 1, 0, 0, 0, 1, 1, 1, 0, 0), clock);
    }

    @Test
    void testCounter() throws IOException {
        var simulator = buildSimulator("counter.nx");
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

    @Test
    void testBundle() throws IOException {
        var simulator = buildSimulator("bundle.nx");
        Function<Integer, String> valid = i -> String.format("pin[%d].valid", i);
        Function<Integer, String> load = i -> String.format("pin[%d].load", i);

        Random random = new Random();
        var high = BitVector.fromBoolean(true);
        for (int i = 0; i < 4; i++) {
            simulator.setInput(valid.apply(i), high);
        }
        for (int i = 0; i < 1000; i++) {
            int sum = 0;
            for (int j = 0; j < 4; j++) {
                int loadValue = random.nextInt(114514);
                simulator.setInput(load.apply(j), BitVector.fromInt(loadValue));
                sum += loadValue;
            }
            simulator.run();
            assertEquals(high, simulator.getOutput("pout.valid"));
            assertEquals(BitVector.fromInt(sum), simulator.getOutput("pout.load"));
        }
    }

    private static Simulator buildSimulator(String other) throws IOException {
        var design = Builder.forExpanded().loadPath(TEST_DIR.resolve(other)).compile();
        var circuit = HFrontend.load(design).parse();
        return new Simulator(circuit);
    }
}
