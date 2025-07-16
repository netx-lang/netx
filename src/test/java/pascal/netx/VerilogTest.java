package pascal.netx;

import org.junit.jupiter.api.Test;
import pascal.netx.backend.verilog.VerilogTransformer;
import pascal.netx.compiler.io.Configuration;
import pascal.netx.compiler.io.NXBuilder;
import pascal.netx.util.print.NotepadWriter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class VerilogTest {
    private static final boolean GENERATE_EXPECTED = false;
    private static final Path TEST_DIR = Path.of("src/test/resources/testcases/");
    private static final Path EXPECTED_DIR = Path.of("src/test/resources/testcases/verilog/");

    @Test
    void testAdder() throws IOException {
        test("adder");
    }

    @Test
    void testDivideBy3() throws IOException {
        test("divide_by_3");
    }

    @Test
    void testSimpleTimer() throws IOException {
        test("simple_timer");
    }

    @Test
    void testNestedType() throws IOException {
        test("nested_type");
    }

    private static void test(String other) throws IOException {
        var design = NXBuilder.from(Configuration.asDefault())
                .loadPath(TEST_DIR.resolve(other + ".nx")).compileAndDump();
        if (GENERATE_EXPECTED) {
            var notepad = NotepadWriter.pathOut(EXPECTED_DIR.resolve(other + ".v"));
            new VerilogTransformer(notepad, design).parse();
        } else {
            var notepad = NotepadWriter.strOut();
            new VerilogTransformer(notepad, design).parse();
            assertEquals(Files.readString(EXPECTED_DIR.resolve(other + ".v")), notepad.toString());
        }
    }
}
