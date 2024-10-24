package pascal.hardscript.compiler.io.parser;

import org.antlr.v4.runtime.CharStreams;
import org.junit.jupiter.api.Test;
import pascal.hardscript.compiler.ast.Identifier;
import pascal.hardscript.compiler.ast.expr.access.AccessExpr;
import pascal.hardscript.compiler.ast.expr.access.BitSelector;
import pascal.hardscript.compiler.ast.expr.access.PartSelector;
import pascal.hardscript.compiler.ast.expr.atom.IdentifierExpr;
import pascal.hardscript.compiler.ast.expr.atom.IntNumber;
import pascal.hardscript.compiler.io.Frontend;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HSParserUnitTest {

    @Test
    void testParseAccessExpr() {
        var frontend = Frontend.load(CharStreams.fromString("arr[1][2:0]"));
        var context = frontend.buildParser().access_expression();
        var accessExpr = HSParser.parseAccessExpr(context);
        assertEquals(accessExpr,
                AccessExpr.of(AccessExpr.of(
                    IdentifierExpr.of(Identifier.of("arr")),
                    BitSelector.of(IntNumber.of(1))),
                    PartSelector.of(IntNumber.of(2), IntNumber.of(0))));
    }
}