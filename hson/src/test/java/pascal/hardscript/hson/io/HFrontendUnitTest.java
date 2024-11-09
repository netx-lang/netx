package pascal.hardscript.hson.io;

import org.junit.jupiter.api.Test;
import pascal.hardscript.hson.model.selector.BitSelector;
import pascal.hardscript.hson.model.selector.PartSelector;
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
                DataParser.parseIdentifier("a@(2:9)@1"));
        assertEquals(
                Pair.of(Identifier.of("$i").setSourceLoc(SourceLoc.of(122, 990)), 2),
                DataParser.parseIdentifier("$i@(122:990)@2"));
    }

    @Test
    void testHsonConstant() {
        assertEquals(
                BitVector.parse("111", 3, 2),
                RelationParser.parseConstantValue(FrontendUtils.buildParser("3'h07").constant()));
        assertEquals(
                BitVector.parse("1010", 32, 2),
                RelationParser.parseConstantValue(FrontendUtils.buildParser("10").constant()));
    }

    @Test
    void testHsonSelector() {
        assertEquals(PartSelector.of(1, 2),
                RelationParser.parseSelector(FrontendUtils.buildParser("[1:2]").selector()));
        assertEquals(BitSelector.of(3),
                RelationParser.parseSelector(FrontendUtils.buildParser("[3]").selector()));
    }
}