package pascal.hardscript;

import org.junit.jupiter.api.Test;
import pascal.hardscript.compiler.Configuration;
import pascal.hardscript.compiler.io.HSCompiler;
import pascal.hardscript.compiler.io.HSDumper;
import pascal.hardscript.hson.verilog.VerilogDumper;
import pascal.hardscript.hson.verilog.frontend.HFrontend;
import pascal.hardscript.util.print.NotepadWriter;

import java.io.IOException;
import java.nio.file.Path;

public class VerilogTest {
    private static final Path TEST_DIR = Path.of("src/test/resources/");

    @Test
    void testAdder() throws IOException {
        test("adder");
    }

    @Test
    void testDivideBy3() throws IOException {
        test("divide_by_3");
    }

    private static void test(String other) throws IOException {
        Configuration.FULLY_EXPAND = false;
        var design = HSCompiler.loadPath(TEST_DIR.resolve(other + ".hard")).compile();
        var vModules = HFrontend.load(new HSDumper(design).dump()).parse();
        new VerilogDumper(NotepadWriter.stdOut(), vModules).dump();
    }
}
