package pascal.netx.compiler.io;

import org.antlr.v4.runtime.CharStreams;
import org.junit.jupiter.api.Test;
import pascal.netx.compiler.ast.element.BindingNode;
import pascal.netx.compiler.ast.expr.*;
import pascal.netx.compiler.ast.expr.qualifier.BindingQualifier;
import pascal.netx.compiler.ast.expr.qualifier.GeneratorQualifier;
import pascal.netx.compiler.ast.expr.selector.BitSelector;
import pascal.netx.compiler.ast.expr.selector.PartSelector;
import pascal.netx.util.lang.Identifier;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class NXParserUnitTest {
    private static final Path TEST_DIR = Path.of("src/test/resources/testcases/");

    static NXParser makeParser(String code) {
        return new NXParser(CharStreams.fromString(code), "test.nx");
    }

    @Test
    void testPackage() throws IOException {
        var parser = new NXParser(TEST_DIR.resolve("package.nx"));
        assertNotNull(parser.parse());
    }

    @Test
    void testParseAccessExpr() {
        var parser = makeParser("arr[1][2:0]");
        var context = parser.buildANTLRParser().expression();
        var accessExpr = parser.buildTreeParser().parseExpr(context);
        assertEquals(AccessExpr.of(AccessExpr.of(
                        IdentifierExpr.of(Identifier.of("arr")),
                        BitSelector.of(IntNumber.of(1))),
                PartSelector.of(IntNumber.of(2), IntNumber.of(0))), accessExpr);
    }

    @Test
    void testParseListExpr() {
        var parser = makeParser("[lhs | i in [0..5], let lhs = [1, x[i]]]");
        var context = parser.buildANTLRParser().expression();
        var listExpr = parser.buildTreeParser().parseExpr(context);
        assertEquals(ListComprehension.of(
                IdentifierExpr.of(Identifier.of("lhs")), List.of(
                        GeneratorQualifier.of(
                                Identifier.of("i"),
                                RangeList.of(IntNumber.of(0), IntNumber.of(5))),
                        BindingQualifier.of(BindingNode.of(
                                IdentifierExpr.of(Identifier.of("lhs")),
                                CommonList.of(List.of(
                                        IntNumber.of(1),
                                        AccessExpr.of(
                                                IdentifierExpr.of(Identifier.of("x")),
                                                BitSelector.of(IdentifierExpr.of(Identifier.of("i"))))))
                        )))), listExpr);
    }
}