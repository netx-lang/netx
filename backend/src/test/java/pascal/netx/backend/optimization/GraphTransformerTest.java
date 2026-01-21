package pascal.netx.backend.optimization;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import pascal.netx.util.print.NotepadWriter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class GraphTransformerTest {
    private static final Path TEST_DIR = Path.of("src/test/resources/");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void sampleTest() throws IOException {
        var json = MAPPER.readTree(Files.readString(TEST_DIR.resolve("benchmark/max.json")));
        var transformer = new GraphTransformer(NotepadWriter.pathOut(Path.of("/home/nairen/tmp/test.dot")), json);
        transformer.parse();
        System.out.printf("Before optimization: %s\n", transformer.measure());

        transformer.rewrite();
        transformer.saturate(1, 1);
        System.out.printf(" After optimization: %s\n", transformer.measure());

        transformer.dumpDot();
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

    @Test
    void testBenchmark() throws IOException {
        testBenchmarkCase("fft");
        testBenchmarkCase("rv32i");
        testBenchmarkCase("ADPCM");
        testBenchmarkCase("FIR_8");
        testBenchmarkCase("FMA");
        testBenchmarkCase("SMUL");
        testBenchmarkCase("MCM_3_7_21");
        testBenchmarkCase("MCM_5_93");
        testBenchmarkCase("MCM_7_19_31");
    }

    private void testBenchmarkCase(String name) throws IOException {
        Path path = TEST_DIR.resolve("benchmark").resolve(name + ".json");
        System.out.println("======== " + path.getFileName() + " ========");
        var json = MAPPER.readTree(Files.readString(path));
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
}