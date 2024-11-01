package pascal.hardscript.compiler.analysis;

import org.junit.jupiter.api.Test;
import pascal.hardscript.compiler.io.Frontend;
import pascal.hardscript.util.TestUtils;

import java.io.IOException;
import java.nio.file.Path;


class ASTAnalysisTest {

    private static final Path TEST_DIR = Path.of("src/test/resources/");

    @Test
    void testSourceLocChecker() throws IOException {
        for (var testcase : TestUtils.collectAllFilesUnder(TEST_DIR)) {
            var design = Frontend.loadPath(testcase).buildAST();
            new SourceLocChecker(design).analyze();
        }
    }
}