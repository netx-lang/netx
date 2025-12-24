package pascal.netx.backend.optimization;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import pascal.netx.backend.verilog.VerilogTransformer;
import pascal.netx.util.print.NotepadWriter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class GraphTransformerTest {
    private static final Path TEST_DIR = Path.of("src/test/resources/");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void sampleTest() throws IOException {
        var notepad = NotepadWriter.pathOut(Path.of("/home/nairen/tmp/test.dot"));
//        var notepad = NotepadWriter.stdOut();
        var json = MAPPER.readTree(Files.readString(TEST_DIR.resolve("opt.json")));
        var transformer = new GraphTransformer(notepad, json);
        transformer.parse();
        transformer.rewrite();
        transformer.dump();
        notepad.flush();
    }
}