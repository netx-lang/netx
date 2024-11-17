package pascal.hardscript;

import org.junit.jupiter.api.Test;
import pascal.hardscript.compiler.Configuration;
import pascal.hardscript.compiler.io.HSCompiler;
import pascal.hardscript.compiler.io.HSDumper;
import pascal.hardscript.hson.verilog.VerilogDumper;
import pascal.hardscript.hson.verilog.frontend.HFrontend;
import pascal.hardscript.util.print.NotepadWriter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class VerilogTest {
    private static final boolean GENERATE_EXPECTED = true;
    private static final Path TEST_DIR = Path.of("src/test/resources/");
    private static final Path EXPECTED_DIR = Path.of("src/test/resources/verilog/");

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
        if (GENERATE_EXPECTED) {
            var notepad = NotepadWriter.pathOut(EXPECTED_DIR.resolve(other + ".v"));
            new VerilogDumper(notepad, vModules).dump();
        } else {
            var notepad = NotepadWriter.strOut();
            new VerilogDumper(notepad, vModules).dump();
            assertEquals(Files.readString(EXPECTED_DIR.resolve(other + ".v")), notepad.toString());
        }
    }

    @Test
    void testSimpleTimer() throws IOException {
        test("simple_timer");
    }
}
