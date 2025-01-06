package pascal.netx.compiler.io;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NXCompilerUnitTest {
    private static final Path TEST_DIR = Path.of("src/test/resources/testcases/");

    private static void test(String fileName) throws IOException {
        String content = new String(Files.readAllBytes(TEST_DIR.resolve(fileName)));
        CharStream charStream = CharStreams.fromString(content);
        NXCompiler compiler = Builder.forExpanded().load(charStream);
        assertNotNull(compiler.compile());
    }

    @Test
    void testBuild() throws IOException {
        var compiler = Builder.forExpanded().loadPath(TEST_DIR.resolve("package/_netx.toml"));
        assertNotNull(compiler.compile());
    }

    @Test
    void loadWithValidCharStream() throws IOException {
        var string = Files.readString(TEST_DIR.resolve("bind_1.nx"));
        CharStream charStream = CharStreams.fromString(string);
        NXCompiler compiler = Builder.forExpanded().load(charStream);
        assertNotNull(compiler);
    }

    @Test
    void loadPathWithValidPath() throws IOException {
        NXCompiler compiler = Builder.forExpanded().loadPath(TEST_DIR.resolve("bind_1.nx"));
        assertNotNull(compiler);
    }

    @Test
    void loadPathWithInvalidPath() {
        Path path = Path.of("invalid/path/to/file.nx");
        assertThrows(IOException.class, () -> Builder.forExpanded().loadPath(path));
    }

    @Test
    void testBind() throws IOException {
        test("bind_1.nx");
        test("bind_2.nx");
        test("bind_3.nx");
    }

    @Test
    void testAttr() throws IOException {
        test("attr_1.nx");
        test("attr_2.nx");
        test("attr_3.nx");
    }

    @Test
    void testInvalidInput() {
        CharStream charStream = CharStreams.fromString("invalid input");
        assertThrows(ParseCancellationException.class, () -> Builder.forExpanded().load(charStream));
    }
}