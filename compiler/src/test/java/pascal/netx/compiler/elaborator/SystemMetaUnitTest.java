package pascal.netx.compiler.elaborator;

import org.junit.jupiter.api.Test;
import pascal.netx.compiler.TestUtils;
import pascal.netx.compiler.io.Configuration;
import pascal.netx.compiler.io.NXBuilder;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class SystemMetaUnitTest {
    @Test
    void testLog2() {
        var code = """
                component test : [a] -> [b] {
                    wire a of bit($log2(32));
                    wire b of bit($log2(64));
                    b <> CONCAT <> [1'b0, a];
                }
                """;
        var expected = """
                component test : [a] -> [b] {
                    wire a of bit(5);
                    wire b of bit(6);
                    b <> CONCAT <> [1'b0, a];
                }
                """;
        TestUtils.test(expected, code);
    }

    @Test
    void testPrint() {
        var code = """
                component test {
                    for (i in [0..4]) {
                        for (j in [0..4]) {
                            $print("i = ", i, ", ", "j = ", j);
                        }
                    }
                }
                """;
        assertDoesNotThrow(() -> NXBuilder.from(Configuration.asDefault()).load(code).compileAndDump());
    }

    @Test
    void testContains() {
        var code = """
                component test {
                    let a = [1, 2, 3];
                    let b = [4, 5, 6];
                    if ($contains(a, 2)) then $print("yes");
                    if ($contains(b, 2)) then $print("no");
                }
                """;
        var expected = """
                component test {
                    $print("yes");
                }
                """;
        TestUtils.test(expected, code);
    }

    @Test
    void testEmpty() {
        var code = """
                component test {
                    let a = [];
                    let b = [1, 2, 3];
                    if ($empty(a)) then $print("a is empty");
                    if ($empty(b)) then $print("b is empty");
                }
                """;
        var expected = """
                component test {
                    $print("a is empty");
                }
                """;
        TestUtils.test(expected, code);
    }

    @Test
    void testLength() {
        var code = """
                component test {
                    let a = [1, 2, 3];
                    let b = [];
                    $print("Length of a: ", $length(a));
                    $print("Length of b: ", $length(b));
                }
                """;
        var expected = """
                component test {
                    $print("Length of a: ", 3);
                    $print("Length of b: ", 0);
                }
                """;
        TestUtils.test(expected, code);
    }
}
