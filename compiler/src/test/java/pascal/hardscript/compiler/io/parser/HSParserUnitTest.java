package pascal.hardscript.compiler.io.parser;

import org.antlr.v4.runtime.CharStreams;
import org.junit.jupiter.api.Test;
import pascal.hardscript.compiler.ast.Interpolated;
import pascal.hardscript.compiler.ast.Identifier;
import pascal.hardscript.compiler.ast.expr.access.AccessExpr;
import pascal.hardscript.compiler.ast.expr.access.BitSelector;
import pascal.hardscript.compiler.ast.expr.access.PartSelector;
import pascal.hardscript.compiler.ast.expr.atom.IdentifierExpr;
import pascal.hardscript.compiler.ast.expr.atom.IntNumber;
import pascal.hardscript.compiler.ast.expr.list.CommonList;
import pascal.hardscript.compiler.parser.HardScriptParser;
import pascal.hardscript.compiler.io.Frontend;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HSParserUnitTest {

    static HardScriptParser makeParser(String code) {
        var frontend = Frontend.load(CharStreams.fromString(code));
        return frontend.buildParser();
    }

    @Test
    void testParseIdentifier() {
        var context = makeParser("check_${[a, b]}_this_${man}");
        var identifier = HSParser.parseSymbol(context.symbol());
        assertEquals(identifier,
                Interpolated.of(
                        List.of("check_", "_this_"),
                        List.of(CommonList.of(
                                List.of(IdentifierExpr.of(Identifier.of("a")),
                                        IdentifierExpr.of(Identifier.of("b")))),
                                IdentifierExpr.of(Identifier.of("man")))));
    }

    @Test
    void testParseAccessExpr() {
        var context = makeParser("arr[1][2:0]").access_expression();
        var accessExpr = HSParser.parseAccessExpr(context);
        assertEquals(accessExpr,
                AccessExpr.of(AccessExpr.of(
                    IdentifierExpr.of(Identifier.of("arr")),
                    BitSelector.of(IntNumber.of(1))),
                    PartSelector.of(IntNumber.of(2), IntNumber.of(0))));
    }
}