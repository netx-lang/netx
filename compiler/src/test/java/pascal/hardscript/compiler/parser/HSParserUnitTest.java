package pascal.hardscript.compiler.parser;

import org.antlr.v4.runtime.CharStreams;
import org.junit.jupiter.api.Test;
import pascal.hardscript.compiler.antlr.HardScriptParser;
import pascal.hardscript.compiler.ast.Identifier;
import pascal.hardscript.compiler.ast.Interpolated;
import pascal.hardscript.compiler.ast.expr.*;
import pascal.hardscript.compiler.ast.expr.qualifier.BindingQualifier;
import pascal.hardscript.compiler.ast.expr.qualifier.GeneratorQualifier;
import pascal.hardscript.compiler.ast.expr.selector.BitSelector;
import pascal.hardscript.compiler.ast.expr.selector.PartSelector;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HSParserUnitTest {

    static HardScriptParser makeParser(String code) {
        var frontend = Frontend.load(CharStreams.fromString(code));
        return frontend.buildANTLRParser();
    }

    @Test
    void testParseIdentifier() {
        var context = makeParser("check_${[a, b]}_this_${man}");
        var identifier = HSParser.parseSymbol(context.symbol());
        assertEquals(Interpolated.of(
                        List.of("check_", "_this_"),
                        List.of(CommonList.of(
                                List.of(SymbolExpr.of(Identifier.of("a")),
                                        SymbolExpr.of(Identifier.of("b")))),
                                SymbolExpr.of(Identifier.of("man")))), identifier);
    }

    @Test
    void testParseAccessExpr() {
        var context = makeParser("arr[1][2:0]").expression();
        var accessExpr = HSParser.parseExpr(context);
        assertEquals(AccessExpr.of(AccessExpr.of(
                        SymbolExpr.of(Identifier.of("arr")),
                        BitSelector.of(IntNumber.of(1))),
                PartSelector.of(IntNumber.of(2), IntNumber.of(0))), accessExpr);
    }

    @Test
    void testParseListExpr() {
        var context = makeParser("[port | i <- [0..5], let port = [1, x_${i}]]").expression();
        var listExpr = HSParser.parseExpr(context);
        assertEquals(ListComprehension.of(
                SymbolExpr.of(Identifier.of("port")), List.of(
                        GeneratorQualifier.of(
                                Identifier.of("i"),
                                RangeList.of(IntNumber.of(0), IntNumber.of(5))),
                        BindingQualifier.of(
                                Identifier.of("port"),
                                CommonList.of(List.of(
                                        IntNumber.of(1),
                                        SymbolExpr.of(Interpolated.of(
                                                List.of("x_"),
                                                List.of(SymbolExpr.of(Identifier.of("i")))))))))), listExpr);
    }
}