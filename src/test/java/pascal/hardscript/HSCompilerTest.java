package pascal.hardscript;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import pascal.hardscript.compiler.Configuration;
import pascal.hardscript.compiler.io.HSCompiler;
import pascal.hardscript.compiler.io.HSDumper;
import pascal.hardscript.compiler.io.HSPrinter;
import pascal.hardscript.util.print.NotepadWriter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HSCompilerTest {
    private static final boolean GENERATE_EXPECTED = false;
    private static final Path TEST_DIR = Path.of("src/test/resources/");

    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    void testAdder() throws IOException {
        test("adder");
    }

    @Test
    void testDecoder() throws IOException {
        test("decoder");
    }

    @Test
    void testEncoder() throws IOException {
        test("encoder");
    }

    @Test
    void testDivideBy3() throws IOException {
        test("divide_by_3");
    }

    @Test
    void testRISCV() throws IOException {
        test("riscv");
    }

    private static void testHelper(String fileName) throws IOException {
        var design = HSCompiler.loadPath(TEST_DIR.resolve(fileName + ".hard")).compile();
        var json = new HSDumper(design).dump();

        Path expectedDir = TEST_DIR.resolve(Configuration.FULLY_EXPAND ? "expanded" : "compact");
        if (GENERATE_EXPECTED) {
            var notepad = NotepadWriter.pathOut(expectedDir.resolve(fileName + ".json"));
            new HSPrinter(json, notepad).print();
        } else {
            var expected = mapper.reader()
                    .readTree(Files.newBufferedReader(expectedDir.resolve(fileName + ".json")));
            assertEquals(expected, json);
        }
    }

    public void test(String fileName) throws IOException {
        Configuration.FULLY_EXPAND = true;
        testHelper(fileName);
        Configuration.FULLY_EXPAND = false;
        testHelper(fileName);
    }
}