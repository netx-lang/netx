package pascal.netx.compiler.io;

import org.antlr.v4.runtime.*;
import pascal.netx.compiler.analysis.ast.*;
import pascal.netx.compiler.antlr.NetXLexer;
import pascal.netx.compiler.antlr.NetXParser;
import pascal.netx.compiler.ast.PackageNode;
import pascal.netx.compiler.elaborator.Logger;
import pascal.netx.compiler.exception.ParseCancellationException;

import java.io.IOException;
import java.nio.file.Path;

public final class NXParser {
    private final CharStream charStream;
    private final String fileName;

    public NXParser(Path path) throws IOException {
        this.charStream = CharStreams.fromPath(path);
        this.fileName = path.getFileName().toString();
    }

    public NXParser(CharStream charStream, String fileName) {
        this.charStream = charStream;
        this.fileName = fileName;
    }

    TreeParser buildTreeParser() {
        return new TreeParser(fileName);
    }

    public PackageNode parse() {
        var design = buildTreeParser().parseDesign(buildANTLRParser().design());
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
            Logger.error("ParseCancellation {}:{} {}", line, charPositionInLine, msg);
            throw new ParseCancellationException();
        }
    }
}
