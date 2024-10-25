package pascal.hardscript.compiler.analysis;

import org.junit.jupiter.api.Test;
import pascal.hardscript.compiler.ast.Identifier;
import pascal.hardscript.compiler.parser.Frontend;
import pascal.hardscript.util.TestUtils;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;


class ASTAnalysisTest {

    private static final Path TEST_DIR = Path.of("src/test/resources/");

    @Test
    void testSourceLocChecker() throws IOException {
        for (var testcase : TestUtils.collectAllFilesUnder(TEST_DIR)) {
            var design = Frontend.loadPath(testcase).buildAST();
            new SourceLocChecker(design).analyze();
        }
    }

    @Test
    void testHierarchyAnalysis() throws IOException {
        var design = Frontend.loadPath(TEST_DIR.resolve("adder.hard")).buildAST();
        var analysis = new HierarchyAnalysis(design);
        var result = analysis.analyze();
        assertEquals(result.get(0).getIdentifier(), Identifier.of("full_adder"));
        assertEquals(result.get(1).getIdentifier(), Identifier.of("half_adder"));
    }
}