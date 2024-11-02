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

class FrontendUnitTest {
    @Test
    void loadWithValidCharStream() {
        CharStream charStream = CharStreams.fromString("valid input");
        Frontend frontend = Frontend.load(charStream);
        assertNotNull(frontend);
    }

    @Test
    void loadPathWithValidPath() throws IOException {
        Path path = Path.of("src/test/resources/adder.hard");
        Frontend frontend = Frontend.loadPath(path);
        assertNotNull(frontend);
    }

    @Test
    void loadPathWithInvalidPath() {
        Path path = Path.of("invalid/path/to/file.hard");
        assertThrows(IOException.class, () -> Frontend.loadPath(path));
    }

    @Test
    void parseValidInput() throws IOException {
        String content = new String(Files.readAllBytes(Path.of("src/test/resources/adder.hard")));

        CharStream charStream = CharStreams.fromString(content);
        Frontend frontend = Frontend.load(charStream);
        DesignScope designScope = frontend.compile();
        assertNotNull(designScope);
    }

    @Test
    void elaborateValidInput() throws IOException {
        String content = new String(Files.readAllBytes(Path.of("src/test/resources/divide_by_3.hard")));

        CharStream charStream = CharStreams.fromString(content);
        Frontend frontend = Frontend.load(charStream);
        DesignScope scope = frontend.compile();
        assertNotNull(scope);
    }

    @Test
    void parseInvalidInput() {
        CharStream charStream = CharStreams.fromString("invalid input");
        Frontend frontend = Frontend.load(charStream);
        assertThrows(ParseCancellationException.class, frontend::compile);
    }
}