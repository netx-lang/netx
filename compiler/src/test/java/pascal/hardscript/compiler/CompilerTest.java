package pascal.hardscript.compiler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import pascal.hardscript.compiler.io.Frontend;
import pascal.hardscript.compiler.io.HSPrinter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CompilerTest {
    private static final boolean GENERATE_EXPECTED = false;

    private static final Path TEST_DIR = Path.of("src/test/resources/");
    private static final Path EXPECTED_DIR = Path.of("src/test/resources/expected/");
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
    void testBind() throws IOException {
        test("bind_1");
        test("bind_2");
//        test("bind_3");
    }

    private static void test(String fileName) throws IOException {
        var design = Frontend.loadPath(TEST_DIR.resolve(fileName + ".hard")).elaborate();
        var json = new HSPrinter(design).dump();

//        mapper.writerWithDefaultPrettyPrinter().writeValue(System.out, json);
        if (GENERATE_EXPECTED) {
            mapper.writerWithDefaultPrettyPrinter()
                    .writeValue(EXPECTED_DIR.resolve(fileName + ".json").toFile(), json);
        } else {
            String expected = new String(Files.readAllBytes(EXPECTED_DIR.resolve(fileName + ".json")));
            var actual = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
            assertEquals(expected, actual);
        }

    }
}