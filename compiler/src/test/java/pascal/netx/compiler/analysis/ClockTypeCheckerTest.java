package pascal.netx.compiler.analysis;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import pascal.netx.compiler.io.Configuration;
import pascal.netx.compiler.io.NXBuilder;
import pascal.netx.compiler.utils.TypeCheckingException;

import static org.junit.jupiter.api.Assertions.*;

class ClockTypeCheckerTest {

    private static Object compile(String code) {
        return NXBuilder.from(Configuration.asDefault()).load(code).compileAndDump();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "out <> clk;",
            "out <> NOT <> clk;",
            "out <> CAST(1) <> clk;",
            "out <> UCAST(1) <> clk;",
            "out <> AND <> [clk, 1'b1];",
            "out <> clk[0];",
            "auto intermediate <> clk; out <> intermediate;",
            "auto intermediate <> clk; out <> intermediate[0];",
            "out <> REGISTER(true) <> [clk, clk];"
    })
    void rejectsClockAsDataOnlyInStrictMode(String body) {
        var code = "component top : clk -> out { wire clk of clock(); wire out of bit(1); " + body + " }";
        assertThrows(TypeCheckingException.class, () -> compile(code));
    }

    @Test
    void rejectsConcatenatedClock() {
        var code = """
                component top : clk -> out {
                    wire clk of clock();
                    wire out of bit(2);
                    out <> CONCAT <> [clk, 1'b0];
                }
                """;
        assertThrows(TypeCheckingException.class, () -> compile(code));
    }

    @Test
    void rejectsConversionInsideNestedComponent() {
        var code = """
                component top : clk -> out {
                    wire clk of clock();
                    wire out of bit(1);
                    out <> sub <> clk;
                }
                component sub : clk -> out {
                    wire clk of clock();
                    wire out of bit(1);
                    out <> clk;
                }
                """;
        assertThrows(TypeCheckingException.class, () -> compile(code));
    }

    @ParameterizedTest
    @ValueSource(strings = {"clk", "TO <> clk", "forward <> clk"})
    void preservesClockForwardingAndRegisterUse(String clock) {
        var code = """
                component top : [clk, data] -> out {
                    wire clk of clock();
                    wire data, out of bit(4);
                    out <> REGISTER(true) <> [%s, data];
                }
                component forward : input -> output {
                    auto input, output;
                    output <> input;
                }
                """.formatted(clock);
        assertDoesNotThrow(() -> compile(code));
    }

    @Test
    void preservesConfigurationWhenChangingTop() {
        assertEquals(Configuration.asDefault(), Configuration.builder().build());
    }

    @Test
    void preservesClockDuplicationAndAsynchronousRegister() {
        var code = """
                component top : [clk, rst, data] -> [a, b] {
                    wire clk, c1, c2 of clock();
                    wire rst, data, a, b of bit(1);
                    [c1, c2] <> DUP(2) <> clk;
                    a <> REGISTER(true) <> [c1, data];
                    b <> ASYNC_REGISTER(true, true, 0) <> [c2, rst, data];
                }
                """;
        assertDoesNotThrow(() -> compile(code));
    }

    @Test
    void rejectsDataDrivingClockByDefault() {
        var code = """
                component top : data -> clk {
                    wire clk of clock();
                    wire data of bit(1);
                    clk <> data;
                }
                """;
        assertThrows(TypeCheckingException.class, () -> compile(code));
    }
}
