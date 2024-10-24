package pascal.hardscript.compiler.io;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import pascal.hardscript.compiler.ast.DesignNode;
import pascal.hardscript.compiler.io.parser.HSParser;
import pascal.hardscript.compiler.parser.HardScriptLexer;
import pascal.hardscript.compiler.parser.HardScriptParser;

import java.io.IOException;
import java.nio.file.Path;

public final class Frontend {

    private final CharStream charStream;

    public Frontend(CharStream charStream) {
        this.charStream = charStream;
    }

    public static Frontend load(CharStream charStream) {
        return new Frontend(charStream);
    }

    public static Frontend loadPath(Path path) throws IOException {
        return new Frontend(CharStreams.fromPath(path));
    }

    public HardScriptParser buildParser() {
        var lexer = new HardScriptLexer(charStream);
        lexer.removeErrorListeners();
        lexer.addErrorListener(ThrowingErrorListener.INSTANCE);
        var tokenStream = new CommonTokenStream(lexer);
        var parser = new HardScriptParser(tokenStream);
        parser.removeErrorListeners();
        parser.addErrorListener(ThrowingErrorListener.INSTANCE);
        return parser;
    }

    public DesignNode parse() {
        return HSParser.parseDesign(buildParser().design());
    }

    private static class ThrowingErrorListener extends BaseErrorListener {

        public static final ThrowingErrorListener INSTANCE = new ThrowingErrorListener();

        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                                int line, int charPositionInLine,
                                String msg, RecognitionException e) throws ParseCancellationException {
            throw new ParseCancellationException("line " + line
                    + ":" + charPositionInLine + " " + msg);
        }
    }
}
