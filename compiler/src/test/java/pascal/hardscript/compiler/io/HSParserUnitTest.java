package pascal.hardscript.compiler.io;

import org.antlr.v4.runtime.CharStreams;
import org.junit.jupiter.api.Test;
import pascal.hardscript.compiler.antlr.HardScriptParser;
import pascal.hardscript.compiler.ast.component.BindingNode;
import pascal.hardscript.compiler.ast.expr.*;
import pascal.hardscript.compiler.ast.expr.qualifier.BindingQualifier;
import pascal.hardscript.compiler.ast.expr.qualifier.GeneratorQualifier;
import pascal.hardscript.compiler.ast.expr.selector.BitSelector;
import pascal.hardscript.compiler.ast.expr.selector.PartSelector;
import pascal.hardscript.util.lang.Identifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HSParserUnitTest {

    static HardScriptParser makeParser(String code) {
        var frontend = Builder.forExpanded().load(CharStreams.fromString(code));
        return frontend.buildANTLRParser();
    }

    @Test
    void testParseIdentifier() {
        var context = makeParser("check_${[a, b]}_this_${man}");
        var identifier = HSParser.parsePrimarySymbol(context.primary_symbol());
        assertEquals(InterpolatedSymbol.of(
                List.of("check_", "_this_"),
                List.of(CommonList.of(
                                List.of(IdentifierSymbol.of(Identifier.of("a")),
                                        IdentifierSymbol.of(Identifier.of("b")))),
                        IdentifierSymbol.of(Identifier.of("man")))), identifier);
    }

    @Test
    void testParseAccessExpr() {
        var context = makeParser("arr[1][2:0]").expression();
        var accessExpr = HSParser.parseExpr(context);
        assertEquals(AccessExpr.of(AccessExpr.of(
                        IdentifierSymbol.of(Identifier.of("arr")),
                        BitSelector.of(IntNumber.of(1))),
                PartSelector.of(IntNumber.of(2), IntNumber.of(0))), accessExpr);
    }

    @Test
    void testParseListExpr() {
        var context = makeParser("[port | i in [0..5], let port = [1, x_${i}]]").expression();
        var listExpr = HSParser.parseExpr(context);
        assertEquals(ListComprehension.of(
                IdentifierSymbol.of(Identifier.of("port")), List.of(
                        GeneratorQualifier.of(
                                IdentifierSymbol.of(Identifier.of("i")),
                                RangeList.of(IntNumber.of(0), IntNumber.of(5))),
                        BindingQualifier.of(BindingNode.of(
                                IdentifierSymbol.of(Identifier.of("port")),
                                CommonList.of(List.of(
                                        IntNumber.of(1),
                                        InterpolatedSymbol.of(
                                                List.of("x_"),
                                                List.of(IdentifierSymbol.of(Identifier.of("i")))))))))), listExpr);
    }
}