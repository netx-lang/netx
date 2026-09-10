package pascal.netx;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import pascal.netx.backend.verilog.VerilogTransformer;
import pascal.netx.compiler.io.Configuration;
import pascal.netx.compiler.io.NXBuilder;
import pascal.netx.util.print.NotepadWriter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VerilogTest {
    private static final boolean GENERATE_EXPECTED = true;
    private static final Path TEST_DIR = Path.of("src/test/resources/testcases/");
    private static final Path EXPECTED_DIR = Path.of("src/test/resources/testcases/verilog/");

    @Test
    void testAdder() throws IOException {
        test("adder");
    }

    @Test
    void testDivideBy3() throws IOException {
        test("divide_by_3");
    }

    @Test
    void testSimpleTimer() throws IOException {
        test("simple_timer");
    }

    @Test
    void testNestedType() throws IOException {
        test("nested_type");
    }

    @Test
    void testAsync() throws IOException {
        test("async");
    }

    @Test
    void testRegisterOutputSelector() throws IOException {
        var design = new ObjectMapper().readTree("""
                {
                  "design": {
                    "type": "component",
                    "id": "top",
                    "loc": "test.nx:1:0",
                    "input": ["clk@0", "in@1"],
                    "output": ["out@2"],
                    "members": [
                      { "type": "clock", "id": "clk@0", "loc": "test.nx:2:9" },
                      { "type": "wire", "width": 1, "id": "in@1", "loc": "test.nx:2:14" },
                      { "type": "wire", "width": 4, "id": "out@2", "loc": "test.nx:2:18" },
                      {
                        "type": "builtin",
                        "id": "REGISTER",
                        "loc": "test.nx:3:7",
                        "input": ["clk@0", "in@1"],
                        "output": ["out@2[3]"],
                        "attribute": { "pos_edge": "true" }
                      }
                    ]
                  }
                }
                """);
        var notepad = NotepadWriter.strOut();
        new VerilogTransformer(notepad, design).parse();
        assertTrue(notepad.toString().contains("always @(posedge _0) __reg__2[3] <= _1;"));
    }

    private static void test(String other) throws IOException {
        var design = NXBuilder.from(Configuration.asDefault())
                .load(TEST_DIR.resolve(other + ".nx")).compileAndDump();
        if (GENERATE_EXPECTED) {
            var notepad = NotepadWriter.pathOut(EXPECTED_DIR.resolve(other + ".v"));
            new VerilogTransformer(notepad, design).parse();
        } else {
            var notepad = NotepadWriter.strOut();
            new VerilogTransformer(notepad, design).parse();
            assertEquals(Files.readString(EXPECTED_DIR.resolve(other + ".v")), notepad.toString());
        }
    }
}
