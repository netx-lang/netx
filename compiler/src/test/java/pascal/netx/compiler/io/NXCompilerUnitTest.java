package pascal.netx.compiler.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.junit.jupiter.api.Test;
import pascal.netx.compiler.exception.ParseCancellationException;
import pascal.netx.util.print.NotepadWriter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class NXCompilerUnitTest {
    private static final Path TEST_DIR = Path.of("src/test/resources/testcases/");
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final boolean GENERATE_EXPECTED = false;

    private static void testNonNull(String fileName) throws IOException {
        String content = new String(Files.readAllBytes(TEST_DIR.resolve(fileName)));
        CharStream charStream = CharStreams.fromString(content);
        NXCompiler compiler = NXBuilder.from(Configuration.asDefault()).load(charStream);
        assertNotNull(compiler.compileAndDump());
    }

    private static void testEquals(String testcase, String output) throws IOException {
        Configuration config = Configuration.asDefault();
        if (GENERATE_EXPECTED) {
            var builder = NXBuilder.from(config);
            var compiler = builder.loadPath(TEST_DIR.resolve(testcase));
            builder.printer(NotepadWriter.pathOut(TEST_DIR.resolve(output))).print(compiler.compileAndDump());
        } else {
            var compiler = NXBuilder.from(config).loadPath(TEST_DIR.resolve(testcase));
            var expected = MAPPER.reader().readTree(Files.readString(TEST_DIR.resolve(output)));
            assertEquals(expected, compiler.compileAndDump());
        }
    }

    @Test
    void testProjectBuild() throws IOException {
        testEquals("project/_netx.toml", "expected/project.json");
    }

    @Test
    void testEnum() throws IOException {
        testEquals("enum.nx", "expected/enum.json");
    }

    @Test
    void loadWithValidCharStream() throws IOException {
        var string = Files.readString(TEST_DIR.resolve("bind_1.nx"));
        CharStream charStream = CharStreams.fromString(string);
        NXCompiler compiler = NXBuilder.from(Configuration.asDefault()).load(charStream);
        assertNotNull(compiler);
    }

    @Test
    void loadPathWithValidPath() throws IOException {
        NXCompiler compiler = NXBuilder.from(Configuration.asDefault())
                .loadPath(TEST_DIR.resolve("bind_1.nx"));
        assertNotNull(compiler);
    }

    @Test
    void loadPathWithInvalidPath() {
        Path path = Path.of("invalid/path/to/file.nx");
        assertThrows(IOException.class, () -> NXBuilder.from(Configuration.asDefault()).loadPath(path));
    }

    @Test
    void testBind() throws IOException {
        testNonNull("bind_1.nx");
        testNonNull("bind_2.nx");
        testNonNull("bind_3.nx");
    }

    @Test
    void testAttr() throws IOException {
        testNonNull("attr_1.nx");
        testNonNull("attr_2.nx");
        testNonNull("attr_3.nx");
    }

    @Test
    void testInvalidInput() {
        CharStream charStream = CharStreams.fromString("invalid input");
        assertThrows(ParseCancellationException.class,
                () -> NXBuilder.from(Configuration.asDefault()).load(charStream));
    }
}