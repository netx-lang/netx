package pascal.hardscript.compiler.io;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.junit.jupiter.api.Test;
import pascal.hardscript.compiler.Configuration;
import pascal.hardscript.compiler.model.scope.DesignScope;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HSCompilerUnitTest {
    private static final Path TEST_DIR = Path.of("src/test/resources");

    @Test
    void loadWithValidCharStream() {
        CharStream charStream = CharStreams.fromString("valid input");
        HSCompiler compiler = HSCompiler.load(charStream);
        assertNotNull(compiler);
    }

    @Test
    void loadPathWithValidPath() throws IOException {
        HSCompiler compiler = HSCompiler.loadPath(TEST_DIR.resolve("bind_1.hard"));
        assertNotNull(compiler);
    }

    @Test
    void loadPathWithInvalidPath() {
        Path path = Path.of("invalid/path/to/file.hard");
        assertThrows(IOException.class, () -> HSCompiler.loadPath(path));
    }

    private static void test(String fileName) throws IOException {
        Configuration.FULLY_EXPAND = true;
        String content = new String(Files.readAllBytes(TEST_DIR.resolve(fileName)));

        CharStream charStream = CharStreams.fromString(content);
        HSCompiler compiler = HSCompiler.load(charStream);
        DesignScope scope = compiler.compile();
        assertNotNull(scope);
    }

    @Test
    void testBind() throws IOException {
        test("bind_1.hard");
        test("bind_2.hard");
        test("bind_3.hard");
    }

    @Test
    void testAttr() throws IOException {
        test("attr_1.hard");
        test("attr_2.hard");
        test("attr_3.hard");
    }

    @Test
    void testInvalidInput() {
        CharStream charStream = CharStreams.fromString("invalid input");
        HSCompiler compiler = HSCompiler.load(charStream);
        assertThrows(ParseCancellationException.class, compiler::compile);
    }
}