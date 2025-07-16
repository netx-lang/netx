package pascal.netx;

import org.junit.jupiter.api.Test;
import pascal.netx.backend.graphviz.DotTransformer;
import pascal.netx.compiler.io.Configuration;
import pascal.netx.compiler.io.NXBuilder;
import pascal.netx.util.print.NotepadWriter;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;

class NXVisualizerTest {
    private static final Path TEST_DIR = Path.of("src/test/resources/testcases/");

    @Test
    void testCounter() throws IOException {
        var compiler = NXBuilder.from(Configuration.asDefault())
                .loadPath(TEST_DIR.resolve("adder.nx"));

        var notepad = NotepadWriter.strOut();
        new DotTransformer(notepad, compiler.compileAndDump(), 2).parse();
        assertFalse(notepad.toString().isBlank());
    }
}