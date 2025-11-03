package pascal.netx.compiler.elaborator;

import org.junit.jupiter.api.Test;

import static pascal.netx.compiler.TestUtils.test;

public class EvaluatorUnitTest {
    @Test
    void testBoolean() {
        var expected = """
                component top : a -> b {
                    wire a, b of bit(4);
                    b <> sub(2, 3, 1) <> a;
                }
                component sub(x, y, z) : a -> b {
                    auto b <> MUL <> [a, a];
                }
                """;
        var actual = """
                component top : a -> b {
                    wire a, b of bit(4);
                    b <> sub(2, 3, 1) <> a;
                }
                component sub(x, y, z) : a -> b {
                    auto if (x < y and x < z) then b <> ADD <> [a, a] else b <> MUL <> [a, a];
                }
                """;
        test(expected, actual);
    }

    @Test
    void testRecursion() {
        var expected = """
                component top : a -> b {
                    wire a, b of bit(4);
                    b <> NOT <> NOT <> NOT <> NOT <> NOT <> NOT <> TO <> a;
                }
                """;
        var actual = """
                component top : a -> b {
                    wire a, b of bit(4);
                    let f(x) = if x == 0 then 1 else x * f(x - 1);
                    let g(x) = if x == 0 then TO else NOT <> g(x - 1);
                    b <> g(f(3)) <> a;
                }
                """;
        test(expected, actual);
    }

    @Test
    void testLetExpr() {
        var expected = """
                component top : [] -> [o] {
                    wire o of bit(32);
                    let x = 1 in
                    let y = 2 in
                    o <> AND <> [x, y];
                }
                """;
        var actual = """
                component top : [] -> [o] {
                    wire o of bit(32);
                    o <> AND <> [1, 2];
                }
                """;
        test(expected, actual);
    }
}
