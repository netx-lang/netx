package pascal.hardscript.compiler;

import com.fasterxml.jackson.databind.ObjectMapper;
import pascal.hardscript.compiler.io.Frontend;
import pascal.hardscript.compiler.io.HSPrinter;

import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CompilerTest extends ResourcesFileTest {
    private static final boolean GENERATE_EXPECTED = false;
    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void test(String fileName) throws IOException {
        var design = Frontend.loadPath(TEST_DIR.resolve(fileName + ".hard")).compile();
        var json = new HSPrinter(design).dump();

        if (GENERATE_EXPECTED) {
            mapper.writerWithDefaultPrettyPrinter()
                    .writeValue(EXPECTED_DIR.resolve(fileName + ".json").toFile(), json);
        } else {
            var expected = mapper.reader()
                    .readTree(Files.newBufferedReader(EXPECTED_DIR.resolve(fileName + ".json")));
            assertEquals(expected, json);
        }
    }
}