package pascal.netx.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import pascal.netx.compiler.NXCompiler;
import pascal.netx.compiler.io.Configuration;
import pascal.netx.compiler.io.Logger;
import pascal.netx.compiler.io.NXBuilder;
import pascal.netx.compiler.model.circuit.scope.ComponentScope;
import pascal.netx.util.print.NotepadWriter;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class TestUtils {
    public static final Path TEST_DIR = Path.of("src/test/resources/testcases/");
    public static final Path EXPECTED_DIR = TEST_DIR.resolve("expected");
    public static final boolean GENERATE_EXPECTED = false;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static List<Path> collectAllFilesUnder(Path dir) throws IOException {
        return collectFilesUnder(dir, _ -> true);
    }

    public static List<Path> collectFilesUnder(Path dir, Predicate<? super Path> predicate) throws IOException {
        try (var paths = Files.list(dir)) {
            return paths.filter(Files::isRegularFile).filter(predicate).toList();
        }
    }

    public static void testSugar(String expected, String actual) {
        var builder = NXBuilder.from(Configuration.asDefault());
        assertEquals(
                eliminateSource(builder.load(expected).compileAndDump()),
                eliminateSource(builder.load(actual).compileAndDump())
        );
    }

    public static <T> void testGen(Path expected, T actual) {
        var builder = NXBuilder.from(Configuration.asDefault());
        try {
            NXCompiler compiler = invokeLoad(builder, actual);
            if (GENERATE_EXPECTED) {
                builder.printer(NotepadWriter.pathOut(expected)).print(compiler.compileAndDump());
            } else {
                assertEquals(
                        eliminateSource(MAPPER.reader().readTree(Files.readString(expected))),
                        eliminateSource(compiler.compileAndDump())
                );
            }
        } catch (Exception e) {
            Logger.error("Error during testGen for " + expected.getFileName() + ": " + e.getMessage());
            fail();
        }
    }

    public static void testGen(String filename) {
        testGen(EXPECTED_DIR.resolve(filename + ".json"), TEST_DIR.resolve(filename + ".nx"));
    }

    public static ComponentScope elaborateSingleComponent(String code) {
        return NXBuilder.from(Configuration.asDefault())
                .load(CharStreams.fromString(code)).elaborate().getTop().getTop();
    }

    public static void compileSingleComponent(String code) {
        NXBuilder.from(Configuration.asDefault())
                .load(CharStreams.fromString(code)).compileAndDump();
    }

    private static JsonNode eliminateSource(JsonNode node) {
        if (node.has("loc")) {
            ((ObjectNode) node).put("loc", "0:0");
        }
        for (JsonNode child : node) {
            eliminateSource(child);
        }
        return node;
    }

    private static NXCompiler invokeLoad(NXBuilder builder, Object actual) throws ReflectiveOperationException {
        Class<?> builderClass = builder.getClass();
        switch (actual) {
            case Path path -> {
                Method m = builderClass.getMethod("load", Path.class);
                return (NXCompiler) m.invoke(builder, path);
            }
            case CharStream charStream -> {
                Method m = builderClass.getMethod("load", CharStream.class);
                return (NXCompiler) m.invoke(builder, charStream);
            }
            case String str -> {
                Method m = builderClass.getMethod("load", String.class);
                return (NXCompiler) m.invoke(builder, str);
            }
            default -> throw new IllegalArgumentException("No matching load method for " + actual.getClass().getName());
        }
    }
}
