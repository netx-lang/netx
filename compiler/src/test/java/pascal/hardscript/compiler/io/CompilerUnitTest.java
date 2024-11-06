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

class CompilerUnitTest {
    @Test
    void loadWithValidCharStream() {
        CharStream charStream = CharStreams.fromString("valid input");
        Compiler compiler = Compiler.load(charStream);
        assertNotNull(compiler);
    }

    @Test
    void loadPathWithValidPath() throws IOException {
        Path path = Path.of("src/test/resources/adder.hard");
        Compiler compiler = Compiler.loadPath(path);
        assertNotNull(compiler);
    }

    @Test
    void loadPathWithInvalidPath() {
        Path path = Path.of("invalid/path/to/file.hard");
        assertThrows(IOException.class, () -> Compiler.loadPath(path));
    }

    @Test
    void parseValidInput() throws IOException {
        String content = new String(Files.readAllBytes(Path.of("src/test/resources/adder.hard")));

        CharStream charStream = CharStreams.fromString(content);
        Compiler compiler = Compiler.load(charStream);
        DesignScope designScope = compiler.compile();
        assertNotNull(designScope);
    }

    @Test
    void elaborateValidInput() throws IOException {
        String content = new String(Files.readAllBytes(Path.of("src/test/resources/divide_by_3.hard")));

        CharStream charStream = CharStreams.fromString(content);
        Compiler compiler = Compiler.load(charStream);
        DesignScope scope = compiler.compile();
        assertNotNull(scope);
    }

    @Test
    void parseInvalidInput() {
        CharStream charStream = CharStreams.fromString("invalid input");
        Compiler compiler = Compiler.load(charStream);
        assertThrows(ParseCancellationException.class, compiler::compile);
    }
}