package pascal.hardscript.compiler.analysis;

import org.junit.jupiter.api.Test;
import pascal.hardscript.compiler.io.Frontend;
import pascal.hardscript.compiler.io.HSParser;
import pascal.hardscript.compiler.io.HSPrinter;
import pascal.hardscript.util.TestUtils;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;


class AnalysisTest {

    private static final Path TEST_DIR = Path.of("src/test/resources/");

    @Test
    void testSourceLocChecker() throws IOException {
        for (var testcase : TestUtils.collectAllFilesUnder(TEST_DIR)) {
            var frontend = Frontend.loadPath(testcase);
            var design = HSParser.parseDesign(frontend.buildANTLRParser().design());
            new SourceLocChecker(design).analyze();
        }
    }

    @Test
    void testDataTypeInference() {
        var expected = """
                component top {
                    inst clk of clock()
                    inst a of wire(bit(4))
                    inst b of wire(bit(4))
                    inst x of wire(bit(4))
                    inst c of wire(bit(2))
                    inst d of wire(bit(2))
                    [a, b] -> OR -> [x]
                    [a[1:0], b[1:0]] -> AND <> REGISTER(clk, true, c) <> NOT -> [d]
                }
                """;
        var actual = """
                component top {
                    inst clk of clock()
                    inst a of wire(bit(4))
                    inst b of wire(auto)
                    inst x of wire(auto)
                    inst c of wire(auto)
                    inst d of wire(auto)
                    [a, b] -> OR -> [x]
                    [a[1:0], b[1:0]] -> AND <> REGISTER(clk, true, c) <> NOT -> [d]
                }
                """;
        assertEquals(
                new HSPrinter(Frontend.load(expected).compile()).dump(),
                new HSPrinter(Frontend.load(actual).compile()).dump());
    }
}