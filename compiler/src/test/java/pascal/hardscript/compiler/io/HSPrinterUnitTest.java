package pascal.hardscript.compiler.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

class HSPrinterUnitTest {
    // FIXME: check result

    private static final Path TEST_DIR = Path.of("src/test/resources/");
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testAdder() throws IOException {
        var design = Frontend.loadPath(TEST_DIR.resolve("adder.hard")).elaborate();
        var json = new HSPrinter(design).dump();
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(System.out, json);
    }

    @Test
    void testDecoder() throws IOException {
        var design = Frontend.loadPath(TEST_DIR.resolve("decoder.hard")).elaborate();
        var json = new HSPrinter(design).dump();
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(System.out, json);
    }

    @Test
    void testEncoder() throws IOException {
        var design = Frontend.loadPath(TEST_DIR.resolve("encoder.hard")).elaborate();
        var json = new HSPrinter(design).dump();
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(System.out, json);
    }
}