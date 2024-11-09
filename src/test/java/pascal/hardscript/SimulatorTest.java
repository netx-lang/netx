package pascal.hardscript;

import org.junit.jupiter.api.Test;
import pascal.hardscript.compiler.io.Compiler;
import pascal.hardscript.compiler.io.HSPrinter;
import pascal.hardscript.hson.io.Frontend;
import pascal.hardscript.hson.simulator.Simulator;
import pascal.hardscript.util.lang.BitVector;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SimulatorTest {
    private static final Path TEST_DIR = Path.of("src/test/resources/");

    @Test
    void testAdder() throws IOException {
        var design = Compiler.loadPath(TEST_DIR.resolve("adder.hard")).compile();
        var circuit = Frontend.load(new HSPrinter(design).dump()).parse();
        var simulator = new Simulator(circuit);

        var high = BitVector.fromBoolean(true);
        var low = BitVector.fromBoolean(false);

        simulator.setInputValue("a", high);
        simulator.setInputValue("b", high);
        simulator.setInputValue("cin", high);
        simulator.run();
        assertEquals(high, simulator.getOutputValue("out"));
        assertEquals(high, simulator.getOutputValue("cout"));
    }
}
