package pascal.hardscript.compiler.io;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import pascal.hardscript.compiler.analysis.AliasAnalysis;
import pascal.hardscript.compiler.analysis.DataTypeInference;
import pascal.hardscript.compiler.analysis.DuplicatedElimination;
import pascal.hardscript.compiler.analysis.SourceLocChecker;
import pascal.hardscript.compiler.antlr.HardScriptLexer;
import pascal.hardscript.compiler.antlr.HardScriptParser;
import pascal.hardscript.compiler.ast.DesignNode;
import pascal.hardscript.compiler.elaborator.Elaborator;
import pascal.hardscript.compiler.model.scope.DesignScope;

import java.io.IOException;
import java.nio.file.Path;

public final class HSCompiler {

    private final CharStream charStream;

    public HSCompiler(CharStream charStream) {
        this.charStream = charStream;
    }

    public static HSCompiler load(String string) {
        return new HSCompiler(CharStreams.fromString(string));
    }

    public static HSCompiler load(CharStream charStream) {
        return new HSCompiler(charStream);
    }

    public static HSCompiler loadPath(Path path) throws IOException {
        return new HSCompiler(CharStreams.fromPath(path));
    }

    public HardScriptParser buildANTLRParser() {
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
        var design = HSParser.parseDesign(buildANTLRParser().design());
        new SourceLocChecker(design).analyze();
        return design;
    }

    public DesignScope compile() {
        var design = new Elaborator(parse()).elaborate();
        new AliasAnalysis(design).analyze();
        new DuplicatedElimination(design).analyze();
        new DataTypeInference(design).analyze();
        return design;
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
