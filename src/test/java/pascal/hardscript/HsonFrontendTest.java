package pascal.hardscript;

import org.junit.jupiter.api.Test;
import pascal.hardscript.hson.io.Frontend;

import java.io.IOException;
import java.nio.file.Path;


class HsonFrontendTest {
    protected static final Path TEST_DIR = Path.of("src/test/resources/expected/");

    @Test
    void testAdder() throws IOException {
        test("adder");
    }

    @Test
    void testDecoder() throws IOException {
        test("decoder");
    }

    @Test
    void testEncoder() throws IOException {
        test("encoder");
    }

    @Test
    void testDivideBy3() throws IOException {
        test("divide_by_3");
    }

    @Test
    void testBind() throws IOException {
        test("bind_1");
        test("bind_2");
        test("bind_3");
    }

    @Test
    void testRISCV() throws IOException {
        test("riscv");
    }

    public void test(String fileName) throws IOException {
        var circuit = Frontend.loadPath(TEST_DIR.resolve(fileName + ".json")).parse();
        System.out.println(circuit);
    }
}