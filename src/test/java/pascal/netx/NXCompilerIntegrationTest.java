package pascal.netx;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static pascal.netx.test.TestUtils.*;

class NXCompilerIntegrationTest {
    @Disabled
    @Test
    void testProjectBuild() {
        testGen(EXPECTED_DIR.resolve("project.json"), TEST_DIR.resolve("project").resolve("_netx.toml"));
    }

    @Test
    void testAdder() {
        testGen("adder");
    }

    @Test
    void testDecoder() {
        testGen("decoder");
    }

    @Test
    void testEncoder() {
        testGen("encoder");
    }

    @Test
    void testDivideBy3() {
        testGen("divide_by_3");
    }

    @Test
    void testCounter() {
        testGen("counter");
    }

    @Test
    void testBundle() {
        testGen("bundle");
    }

    @Test
    void testArray() {
        testGen("array");
    }

    @Test
    void testNestedType() {
        testGen("nested_type");
    }

    @Test
    void testSimpleTimer() {
        testGen("simple_timer");
    }
}