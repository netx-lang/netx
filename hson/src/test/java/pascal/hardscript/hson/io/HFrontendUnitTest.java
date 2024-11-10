package pascal.hardscript.hson.io;

import org.junit.jupiter.api.Test;
import pascal.hardscript.hson.util.FrontendUtils;
import pascal.hardscript.hson.util.selector.BitSelector;
import pascal.hardscript.hson.util.selector.PartSelector;
import pascal.hardscript.util.lang.BitVector;
import pascal.hardscript.util.lang.Identifier;
import pascal.hardscript.util.lang.SourceLoc;
import pascal.hardscript.util.type.Pair;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HFrontendUnitTest {

    @Test
    void testHsonDataParser() {
        assertEquals(
                Pair.of(Identifier.of("a").setSourceLoc(SourceLoc.of(2, 9)), 1),
                FrontendUtils.parseIdentifier("a@(2:9)@1"));
        assertEquals(
                Pair.of(Identifier.of("$i").setSourceLoc(SourceLoc.of(122, 990)), 2),
                FrontendUtils.parseIdentifier("$i@(122:990)@2"));
    }

    @Test
    void testHsonConstant() {
        assertEquals(
                BitVector.parse("111", 3, 2),
                FrontendUtils.parseConstantValue(FrontendUtils.buildParser("3'h07").constant()));
        assertEquals(
                BitVector.parse("1010", 32, 2),
                FrontendUtils.parseConstantValue(FrontendUtils.buildParser("10").constant()));
    }

    @Test
    void testHsonSelector() {
        assertEquals(PartSelector.of(1, 2),
                FrontendUtils.parseSelector(FrontendUtils.buildParser("[1:2]").selector()));
        assertEquals(BitSelector.of(3),
                FrontendUtils.parseSelector(FrontendUtils.buildParser("[3]").selector()));
    }
}