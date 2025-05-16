package pascal.netx;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import pascal.netx.compiler.io.Configuration;
import pascal.netx.compiler.io.NXBuilder;
import pascal.netx.util.print.NotepadWriter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NXCompilerTest {
    private static final boolean GENERATE_EXPECTED = false;
    private static final Path TEST_DIR = Path.of("src/test/resources/testcases/");

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
    void testCounter() throws IOException {
        test("counter");
    }

    @Test
    void testBundle() throws IOException {
        test("bundle");
    }

    @Test
    void testArray() throws IOException {
        test("array");
    }

    @Test
    void testNestedType() throws IOException {
        test("nested_type");
    }

    @Test
    void testSimpleTimer() throws IOException {
        test("simple_timer");
    }

    private static void testHelper(NXBuilder builder, Path testcase, Path expected) throws IOException {
        var json = builder.loadPath(testcase).compileAndDump();
        if (GENERATE_EXPECTED) {
            var notepad = NotepadWriter.pathOut(expected);
            builder.printer(notepad).print(json);
        } else {
            assertEquals(mapper.reader().readTree(Files.newBufferedReader(expected)), json);
        }
    }

    public void test(String fileName) throws IOException {
        Path testcase = TEST_DIR.resolve(fileName + ".nx");
        Path expected = TEST_DIR.resolve("expanded").resolve(fileName + ".json");
        testHelper(NXBuilder.from(Configuration.forExpandedTest()), testcase, expected);
        expected = TEST_DIR.resolve("compact").resolve(fileName + ".json");
        testHelper(NXBuilder.from(Configuration.forCompactTest()), testcase, expected);
    }
}