package pascal.netx.backend.optimization;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import pascal.netx.util.print.NotepadWriter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class GraphTransformerTest {
    private static final Path TEST_DIR = Path.of("src/test/resources/");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void sampleTest() throws IOException {
        var json = MAPPER.readTree(Files.readString(TEST_DIR.resolve("opt.json")));
        var transformer = new GraphTransformer(NotepadWriter.stdOut(), json);
        transformer.parse();
        System.out.printf("Before optimization: %s\n", transformer.measure());

//        transformer.rewrite();
//        transformer.saturate(1, 1);
        System.out.printf(" After optimization: %s\n", transformer.measure());

        transformer.dump();
    }
}