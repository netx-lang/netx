package pascal.hardscript.compiler.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

class HSPrinterUnitTest {
    private static final Path TEST_DIR = Path.of("src/test/resources/");
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testDump() throws IOException {
        var design = Frontend.loadPath(TEST_DIR.resolve("adder.hard")).elaborate();
        var json = new HSPrinter(design).dump();
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(System.out, json);
    }
}