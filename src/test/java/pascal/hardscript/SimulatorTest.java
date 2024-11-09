package pascal.hardscript;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import pascal.hardscript.compiler.io.Compiler;
import pascal.hardscript.compiler.io.HSPrinter;
import pascal.hardscript.hson.io.Frontend;
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
        simulator.setInputValue("a", high);
        simulator.setInputValue("b", high);
        simulator.setInputValue("cin", high);
        simulator.run();
        assertEquals(high, simulator.getOutputValue("out"));
        assertEquals(high, simulator.getOutputValue("cout"));
    }

    @Disabled
    @Test
    void testDivideBy3() throws IOException {
        var simulator = buildSimulator("divide_by_3.hard");
        List<Boolean> clock = new LinkedList<>();
        simulator.setOnEachTick(env ->
                clock.add(env.getOutputValue("clk_3").toBoolean()));
        simulator.runUntil(20);
        System.out.println(clock);
    }

    private static Simulator buildSimulator(String other) throws IOException {
        var design = Compiler.loadPath(TEST_DIR.resolve(other)).compile();
        var circuit = Frontend.load(new HSPrinter(design).dump()).parse();
        return new Simulator(circuit);
    }
}
