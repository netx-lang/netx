package pascal.hardscript.compiler;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

abstract class ResourcesFileTest {
    protected static final Path TEST_DIR = Path.of("src/test/resources/");
    protected static final Path EXPECTED_DIR = Path.of("src/test/resources/expected/");

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

    public abstract void test(String fileName) throws IOException;
}
