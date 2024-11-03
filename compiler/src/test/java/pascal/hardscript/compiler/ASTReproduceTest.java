package pascal.hardscript.compiler;

import pascal.hardscript.compiler.io.Frontend;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ASTReproduceTest extends ResourcesFileTest {
    public void test(String fileName) throws IOException {
        var expected = Frontend.loadPath(TEST_DIR.resolve(fileName + ".hard")).parse().toString();
        var actual = Frontend.load(expected).parse().toString();
        assertEquals(expected, actual);
    }
}