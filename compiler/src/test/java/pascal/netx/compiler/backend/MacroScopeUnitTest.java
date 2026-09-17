package pascal.netx.compiler.backend;

import org.junit.jupiter.api.Test;
import pascal.netx.compiler.model.ast.expr.IdentifierExpr;
import pascal.netx.compiler.model.circuit.scope.ComponentScope;
import pascal.netx.compiler.utils.CompileCancellationException;
import pascal.netx.util.lang.Identifier;

import static org.junit.jupiter.api.Assertions.*;
import static pascal.netx.test.TestUtils.elaborateSingleComponent;
import static pascal.netx.test.TestUtils.testSugar;

class MacroScopeUnitTest {
    private static int value(ComponentScope scope, String name) {
        return scope.getEvaluator().evaluateToInt(IdentifierExpr.of(Identifier.of(name)));
    }

    private static void assertResult(int expected, String bindings) {
        var scope = elaborateSingleComponent("component top : [] -> [] { " + bindings + " }");
        assertEquals(expected, value(scope, "result"));
    }

    @Test
    void capturesTransitivelyBeforeLaterShadowing() {
        assertResult(32, """
                let i = 5;
                let width = 2 ** i;
                let result = width;
                let i = 2;
                let width = 99;
                """);
    }

    @Test
    void callerParametersDoNotAffectCalleeCaptures() {
        assertResult(35, """
                let i = 5;
                let width = 2 ** i;
                let helper(i) = width + i;
                let result = helper(3);
                """);
    }

    @Test
    void ordinaryBindingUsesPreviousBinding() {
        assertResult(7, """
                let sum = 5;
                let sum = sum + 2;
                let result = sum;
                """);
    }

    @Test
    void rejectsUnboundAndForwardReferencesAtDefinition() {
        for (var bindings : new String[] {
                "let x = x + 1;",
                "let x = missing; let missing = 1;",
                "let f(n) = g(n); let g(n) = f(n);",
                "let f(n) = let g(x) = x in x;",
                "let f(n) = [x | x in x];"
        }) {
            assertThrows(CompileCancellationException.class,
                    () -> elaborateSingleComponent("component top { " + bindings + " }"), bindings);
        }
    }

    @Test
    void selfRecursionSurvivesShadowingAndUsesFreshParameters() {
        assertResult(30, """
                let f(n) = if n == 0 then 1 else n * f(n - 1);
                let saved(n) = f(n) + f(n - 1);
                let f(n) = 100;
                let result = saved(4);
                """);
    }

    @Test
    void nestedLetCapturesOuterInitializerAndRestoresParameterScope() {
        assertResult(24, """
                let x = 10;
                let f(n) = (let x = x + n in let g(x) = x + n in g(x)) + x;
                let x = 100;
                let result = f(2);
                """);
    }

    @Test
    void nestedRecursiveMacroCapturesItsInvocation() {
        assertResult(9, """
                let outer(n) = let inner(k) = if k == 0 then n else inner(k - 1) + n in inner(2);
                let result = outer(3);
                """);
    }

    @Test
    void comprehensionCapturesGeneratorSourceBeforeBindingAndEachIteration() {
        assertResult(15, """
                let i = [2, 3];
                let values = [f(1) | i in i, let f(n) = i + n];
                let i = [100];
                let result = values[0] + values[1] + (let x = 4 in [x | x in [x..x+1]][0]) + 4;
                """);
    }

    @Test
    void loopMacrosCaptureCurrentIterationAndOuterMacrosStayFixed() {
        testSugar("""
                component top : [] -> a {
                    wire a[4] of bit(32);
                    a[0] <> 32;
                    a[1] <> 33;
                    a[2] <> 34;
                    a[3] <> 35;
                }
                """, """
                component top : [] -> a {
                    wire a[4] of bit(32);
                    let i = 5;
                    let width = 2 ** i;
                    for i in [0..4] {
                        let offset = i;
                        let i = 99;
                        a[offset] <> width + offset;
                    }
                }
                """);
    }

    @Test
    void capturedComponentCallsKeepIndependentAttributes() {
        var sub = """
                component sub(n) : [] -> o {
                    wire o of bit(32);
                    o <> n;
                }
                """;
        testSugar("""
                component top : [] -> [a, b] {
                    wire a, b of bit(32);
                    [a, b] <> [sub(2), sub(3)];
                }
                """ + sub, """
                component top : [] -> [a, b] {
                    wire a, b of bit(32);
                    let make(n) = sub(n);
                    let sub(n) = 99;
                    [a, b] <> [make(2), make(3)];
                }
                """ + sub);
    }

    @Test
    void capturedBuiltinCallsKeepIndependentAttributes() {
        testSugar("""
                component top : [clk, a] -> [b, c] {
                    wire clk of clock();
                    wire a, b, c of bit(4);
                    [b, c] <> [REGISTER(true), REGISTER(false)] <> [clk, a, clk, a];
                }
                """, """
                component top : [clk, a] -> [b, c] {
                    wire clk of clock();
                    wire a, b, c of bit(4);
                    let reg(edge) = REGISTER(edge);
                    let REGISTER(edge) = TO;
                    [b, c] <> [reg(true), reg(false)] <> [clk, a, clk, a];
                }
                """);
    }

    @Test
    void capturesAttributesSeparatelyForEachComponentInstance() {
        var top = """
                component top : [] -> [a, b] {
                    wire a, b of bit(32);
                    [a, b] <> [sub(2), sub(5)];
                }
                """;
        testSugar(top + """
                component sub(n) : [] -> o {
                    wire o of bit(32);
                    o <> n;
                }
                """, top + """
                component sub(n) : [] -> o {
                    wire o of bit(32);
                    let saved = n;
                    let n = 99;
                    o <> saved;
                }
                """);
    }

    @Test
    void lazyExpansionCreatesFreshRegistersAndRetainsCapturedWire() {
        testSugar("""
                component top : [clk, a] -> b {
                    wire clk of clock();
                    wire a, b of bit(4);
                    b <> (REGISTER(pos_edge=true) <> [clk#, TO])
                      <> (REGISTER(pos_edge=true) <> [clk#, TO]) <> a;
                }
                """, """
                component top : [clk, a] -> b {
                    wire clk of clock();
                    wire a, b of bit(4);
                    let reg = REGISTER(pos_edge=true) <> [clk#, TO];
                    let chain(clk) = reg <> reg;
                    b <> chain(0) <> a;
                }
                """);
    }

    @Test
    void namedPortLabelsDoNotRemoveSameNamedFreeValues() {
        var sub = """
                component sub : a -> b {
                    wire a, b of bit(32);
                    b <> a;
                }
                """;
        testSugar("""
                component top : a -> b {
                    wire a, b of bit(32);
                    b <> sub <> { a : a };
                }
                """ + sub, """
                component top : a -> b {
                    wire a, b of bit(32);
                    let f(n) = sub <> { a : a };
                    b <> f(0);
                }
                """ + sub);
    }

    @Test
    void namedArgumentsAreEvaluatedInCallerAndMustMatchParameters() {
        assertResult(7, """
                let n = 5;
                let f(n, m) = n + m;
                let result = f(m=2, n=n);
                """);
        for (var call : new String[] { "f(n=1)", "f(n=1, other=2)", "f(n=1, m=2, other=3)" }) {
            assertThrows(CompileCancellationException.class, () -> assertResult(0,
                    "let m = 99; let f(n, m) = n + m; let result = " + call + ";"));
        }
    }

    @Test
    void lexicalBindingsAreRestoredAfterFailedExpansion() {
        var scope = elaborateSingleComponent("""
                component top {
                    let n = 7;
                    let f(n) = let nested = n in [1][nested];
                    let bad = f(10);
                }
                """);
        int count = scope.getBindingCount();
        assertThrows(RuntimeException.class, () -> value(scope, "bad"));
        assertEquals(count, scope.getBindingCount());
        assertEquals(7, value(scope, "n"));
    }

    @Test
    void capturesQualifiedEnumAndRespectsLocalShadowing() {
        var scope = elaborateSingleComponent("""
                enum Color { RED = 2'b01, BLUE = 2'b10 }
                component top {
                    let saved = Color.RED;
                    let Color = 0;
                    let result = saved;
                }
                """);
        assertEquals(1, value(scope, "result"));
    }
}
