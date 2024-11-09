package pascal.hardscript.compiler.io;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.junit.jupiter.api.Test;
import pascal.hardscript.compiler.model.scope.DesignScope;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HSCompilerUnitTest {
    @Test
    void loadWithValidCharStream() {
        CharStream charStream = CharStreams.fromString("valid input");
        HSCompiler compiler = HSCompiler.load(charStream);
        assertNotNull(compiler);
    }

    @Test
    void loadPathWithValidPath() throws IOException {
        Path path = Path.of("src/test/resources/valid.hard");
        HSCompiler compiler = HSCompiler.loadPath(path);
        assertNotNull(compiler);
    }

    @Test
    void loadPathWithInvalidPath() {
        Path path = Path.of("invalid/path/to/file.hard");
        assertThrows(IOException.class, () -> HSCompiler.loadPath(path));
    }

    @Test
    void testValidInput() throws IOException {
        String content = new String(Files.readAllBytes(Path.of("src/test/resources/valid.hard")));

        CharStream charStream = CharStreams.fromString(content);
        HSCompiler compiler = HSCompiler.load(charStream);
        DesignScope scope = compiler.compile();
        assertNotNull(scope);
    }

    @Test
    void testInvalidInput() {
        CharStream charStream = CharStreams.fromString("invalid input");
        HSCompiler compiler = HSCompiler.load(charStream);
        assertThrows(ParseCancellationException.class, compiler::compile);
    }
}