package pascal.netx.compiler.io;

import org.junit.jupiter.api.Test;

import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;
import static pascal.netx.test.TestUtils.TEST_DIR;

class ProjectConfigReaderUnitTest {
    private static void assertEnumImportWorks(String fixtureName) {
        var compiler = assertDoesNotThrow(() -> NXBuilder.from(Configuration.asDefault())
                .load(TEST_DIR.resolve(fixtureName).resolve("_netx.toml")));
        var dump = assertDoesNotThrow(compiler::compileAndDump);

        assertEquals("top", dump.get("design").get("id").textValue());
        assertEquals("state", dump.get("top_output").get(0).textValue());
        var stateWire = StreamSupport.stream(dump.get("design").get("members").spliterator(), false)
                .filter(member -> member.has("id"))
                .filter(member -> member.get("id").textValue().startsWith("state@"))
                .findFirst()
                .orElse(null);
        assertNotNull(stateWire);
        assertEquals(1, stateWire.get("width").intValue());
    }

    @Test
    void testImportEnumFromSubProject() {
        assertEnumImportWorks("project_enum");
    }

    @Test
    void testWildcardImportEnumFromSubProject() {
        assertEnumImportWorks("project_enum_wildcard");
    }
}
