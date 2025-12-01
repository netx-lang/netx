package pascal.netx.compiler.io;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.junit.jupiter.api.Test;
import pascal.netx.compiler.NXCompiler;
import pascal.netx.compiler.model.ast.element.BindingNode;
import pascal.netx.compiler.model.ast.expr.*;
import pascal.netx.compiler.model.ast.expr.qualifier.BindingQualifier;
import pascal.netx.compiler.model.ast.expr.qualifier.GeneratorQualifier;
import pascal.netx.compiler.model.ast.expr.selector.BitSelector;
import pascal.netx.compiler.model.ast.expr.selector.PartSelector;
import pascal.netx.compiler.utils.ParseCancellationException;
import pascal.netx.util.lang.Identifier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static pascal.netx.test.TestUtils.TEST_DIR;

class NXParserUnitTest {
    static NXParser makeParser(String code) {
        return new NXParser(CharStreams.fromString(code), "test.nx");
    }

    @Test
    void testInvalidInput() {
        CharStream charStream = CharStreams.fromString("invalid input");
        assertThrows(ParseCancellationException.class,
                () -> NXBuilder.from(Configuration.asDefault()).load(charStream));
    }

    @Test
    void loadPathWithInvalidPath() {
        Path path = Path.of("invalid/path/to/file.nx");
        assertThrows(IOException.class, () -> NXBuilder.from(Configuration.asDefault()).load(path));
    }

    @Test
    void loadWithCharStream() throws IOException {
        var string = Files.readString(TEST_DIR.resolve("frege.nx"));
        CharStream charStream = CharStreams.fromString(string);
        NXCompiler compiler = NXBuilder.from(Configuration.asDefault()).load(charStream);
        assertNotNull(compiler);
    }

    @Test
    void loadPathWithPath() throws IOException {
        NXCompiler compiler = NXBuilder.from(Configuration.asDefault())
                .load(TEST_DIR.resolve("frege.nx"));
        assertNotNull(compiler);
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