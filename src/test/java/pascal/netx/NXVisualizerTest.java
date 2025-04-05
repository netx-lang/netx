package pascal.netx;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import pascal.netx.compiler.io.Builder;
import pascal.netx.util.print.NotepadWriter;

import java.io.IOException;
import java.nio.file.Path;

class NXVisualizerTest {
    private static final Path TEST_DIR = Path.of("src/test/resources/testcases/");

    @Disabled
    @Test
    void testCounter() throws IOException {
        var compiler = Builder.forCompact().loadPath(TEST_DIR.resolve("counter.nx"));
        var notepad = NotepadWriter.pathOut(Path.of("a.out"));
        compiler.compileAndVisualize(notepad, 2);
        var runtime = Runtime.getRuntime();
        runtime.exec(new String[]{"dot", "-T", "pdf", "a.out", "-o", "a.pdf"});
    }
}