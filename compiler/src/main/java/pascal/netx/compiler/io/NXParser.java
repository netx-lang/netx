package pascal.netx.compiler.io;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pascal.netx.compiler.analysis.ast.*;
import pascal.netx.compiler.antlr.NetXLexer;
import pascal.netx.compiler.antlr.NetXParser;
import pascal.netx.compiler.ast.PackageNode;

public final class NXParser {
    private static final Logger log = LoggerFactory.getLogger(NXParser.class);
    private final CharStream charStream;

    public NXParser(CharStream charStream) {
        this.charStream = charStream;
    }

    public PackageNode parse() {
        var design = TreeParser.parseDesign(buildANTLRParser().design());
        var manager = new ASTAnalysisManager(design);
        manager.analyze(SourceLocChecker.class);
        manager.analyze(PortExprRotation.class);
        manager.analyze(InterfaceChecker.class);
        manager.analyze(BindingLhsAnalysis.class);
        return design;
    }

    NetXParser buildANTLRParser() {
        var lexer = new NetXLexer(charStream);
        lexer.removeErrorListeners();
        lexer.addErrorListener(ThrowingErrorListener.INSTANCE);
        var tokenStream = new CommonTokenStream(lexer);
        var parser = new NetXParser(tokenStream);
        parser.removeErrorListeners();
        parser.addErrorListener(ThrowingErrorListener.INSTANCE);
        return parser;
    }

    private static class ThrowingErrorListener extends BaseErrorListener {

        public static final ThrowingErrorListener INSTANCE = new ThrowingErrorListener();

        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                                int line, int charPositionInLine,
                                String msg, RecognitionException e) throws ParseCancellationException {
            log.error("ParseCancellation {}:{} {}", line, charPositionInLine, msg);
            throw new ParseCancellationException();
        }
    }
}
