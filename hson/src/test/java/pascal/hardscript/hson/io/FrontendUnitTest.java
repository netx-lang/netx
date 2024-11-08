package pascal.hardscript.hson.io;

import org.junit.jupiter.api.Test;
import pascal.hardscript.util.lang.Identifier;
import pascal.hardscript.util.lang.SourceLoc;

import static org.junit.jupiter.api.Assertions.*;

class FrontendUnitTest {
    @Test
    void testIdentifier() {
        assertEquals(
                Identifier.of("a").setSourceLoc(SourceLoc.of(2, 9)),
                FrontendUtils.parseIdentifier("a@(2:9)"));
        assertEquals(
                Identifier.of("$i").setSourceLoc(SourceLoc.of(122, 990)),
                FrontendUtils.parseIdentifier("$i@(122:990)"));
    }
}