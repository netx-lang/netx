package pascal.hardscript;

import org.junit.jupiter.api.Test;
import pascal.hardscript.hson.verilog.VerilogDumper;
import pascal.hardscript.hson.verilog.frontend.HFrontend;
import pascal.hardscript.util.print.NotepadWriter;

import java.io.IOException;
import java.nio.file.Path;

public class VerilogTest {
    private static final Path TEST_DIR = Path.of("src/test/resources/expected/");

    @Test
    void testAdder() throws IOException {
        var module = HFrontend.loadPath(TEST_DIR.resolve( "adder.json")).parse();
        var notepad = NotepadWriter.stdOut();
        new VerilogDumper(notepad, module.getFirst()).dump();
    }
}
