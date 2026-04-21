package pascal.netx.backend.optimization;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import pascal.netx.util.print.NotepadWriter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class GraphTransformerTest {
    private static final Path TEST_DIR = Path.of("src/test/resources/");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void sampleTest() throws IOException {
        var json = MAPPER.readTree(Files.readString(TEST_DIR.resolve("benchmark/rv32i.json")));
        var transformer = new GraphTransformer(NotepadWriter.strOut(), json);
        transformer.parse();
        System.out.printf("Before optimization: %s\n", transformer.measure());

        transformer.rewrite();
        System.out.println("Finished rewriting.");
        transformer.saturate(1, 0);
        transformer.dumpDot();
        System.out.printf(" After optimization: %s\n", transformer.measure());
    }

    @Test
    void testDumpNxonRoundTrip() throws IOException {
        testRoundTrip("fft");
        testRoundTrip("rv32i");
        testRoundTrip("ADPCM");
        testRoundTrip("FIR_8");
        testRoundTrip("FMA");
        testRoundTrip("SMUL");
        testRoundTrip("MCM_3_7_21");
        testRoundTrip("MCM_5_93");
        testRoundTrip("MCM_7_19_31");
    }

    private void testRoundTrip(String filename) throws IOException {
        var notepad = NotepadWriter.strOut();
        var json = MAPPER.readTree(Files.readString(
                TEST_DIR.resolve("benchmark").resolve(filename + ".json")));
        var transformer = new GraphTransformer(notepad, json);
        transformer.parse();
        transformer.rewrite();
        transformer.saturate(1, 1);
        var dumped = transformer.dumpNxon();
        assertDoesNotThrow(() -> new GraphTransformer(notepad, dumped).parse());
    }

    @Disabled
    @Test
    void testBenchmark() throws IOException {
        try (var files = Files.list(TEST_DIR.resolve("benchmark"))) {
            files.filter(file -> file.getFileName().toString().endsWith(".json"))
                    .forEach(this::testBenchmarkCase);
        }
    }

    private void testBenchmarkCase(Path path) {
        System.out.println("======== " + path.getFileName() + " ========");
        JsonNode json;
        try {
            json = MAPPER.readTree(Files.readString(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        var transformer = new GraphTransformer(NotepadWriter.stdOut(), json);
        transformer.parse();
        System.out.printf("  Before optimization: %s\n", transformer.measure());
        double time = System.currentTimeMillis();
        transformer.rewrite();
        transformer.saturate(1, 1);
        time = System.currentTimeMillis() - time;
        System.out.printf("   After optimization: %s\n", transformer.measure());
        System.out.println("Time Elapsed: " + time + " ms");
        System.out.println();
    }

    @Test
    void testDumpNxonPreservesLocations() throws IOException {
        var json = MAPPER.readTree(Files.readString(TEST_DIR.resolve("benchmark/adder.json")));
        var transformer = new GraphTransformer(NotepadWriter.strOut(), json);
        transformer.parse();
        transformer.rewrite();
        transformer.saturate(1, 1);

        var dumped = transformer.dumpNxon();
        assertFalse(dumped.get("design").get("loc").textValue().isBlank());
        var members = dumped.get("design").get("members");
        boolean hasLocatedBuiltin = false;
        boolean hasNamedBuiltinOutput = false;
        for (var member : members) {
            if (Objects.equals(member.get("type").textValue(), "builtin")
                    && !member.get("loc").textValue().isBlank()) {
                hasLocatedBuiltin = true;
            }
            if (Objects.equals(member.get("type").textValue(), "builtin")) {
                var output = member.get("output").get(0).textValue();
                if (!output.startsWith("$o@")) {
                    hasNamedBuiltinOutput = true;
                }
            }
        }
        assertTrue(hasLocatedBuiltin);
        assertTrue(hasNamedBuiltinOutput);
    }

    @Test
    void testDumpNxonPreservesHierarchyMetadata() throws IOException {
        var json = MAPPER.readTree(Files.readString(TEST_DIR.resolve("benchmark/ADPCM.json")));
        var transformer = new GraphTransformer(NotepadWriter.strOut(), json);
        transformer.parse();
        transformer.rewrite();
        transformer.saturate(1, 1);

        var dumped = transformer.dumpNxon();
        var members = dumped.get("design").get("members");
        boolean hasHierarchicalWire = false;
        boolean hasHierarchicalBuiltin = false;
        for (var member : members) {
            if (Objects.equals(member.get("type").textValue(), "wire")
                    && member.get("id").textValue().contains("/")) {
                hasHierarchicalWire = true;
                assertFalse(member.has("attribute"));
                assertTrue(member.get("id").textValue().contains("/$o@")
                        || member.get("id").textValue().matches(".*/[A-Za-z0-9_$\\[\\]]+@\\d+"));
            }
            if (Objects.equals(member.get("type").textValue(), "builtin")
                    && member.get("output").get(0).textValue().contains("/")) {
                hasHierarchicalBuiltin = true;
                assertFalse(member.has("name"));
                if (member.has("attribute")) {
                    assertFalse(member.get("attribute").has("hierarchy_paths"));
                    assertFalse(member.get("attribute").has("origin_names"));
                }
            }
        }
        assertTrue(hasHierarchicalWire);
        assertTrue(hasHierarchicalBuiltin);
    }
}
