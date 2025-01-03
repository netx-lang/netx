package pascal.netx.compiler.io;

import com.fasterxml.jackson.databind.JsonNode;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import pascal.netx.compiler.analysis.*;
import pascal.netx.compiler.antlr.NetXLexer;
import pascal.netx.compiler.antlr.NetXParser;
import pascal.netx.compiler.ast.DesignNode;
import pascal.netx.compiler.elaborator.Elaborator;
import pascal.netx.compiler.model.scope.GlobalScope;

public final class HSCompiler implements Compiler {
    private final Configuration config;
    private final CharStream charStream;

    public HSCompiler(Configuration config, CharStream charStream) {
        this.config = config;
        this.charStream = charStream;
    }

    private static void checkType(GlobalScope design) {
        new MetaElimination(design).analyze();
        new DataTypeInference(design).analyze();
        new AccessChecker(design).analyze();
        new CompositeDataBlaster(design).analyze();
        new IOAnalysis(design).analyze();
    }

    public DesignNode parse() {
        var design = HSParser.parseDesign(buildANTLRParser().design());
        new SourceLocChecker(design).analyze();
        new InterfaceChecker(design).analyze();
        new BindingLhsAnalysis(design).analyze();
        return design;
    }

    public GlobalScope elaborate() {
        DesignNode node = parse();
        return new Elaborator(node, config).elaborate();
    }

    @Override
    public JsonNode compile() {
        GlobalScope design = elaborate();
        checkType(design);
        if (config.fullyExpand()) {
            new GlobalAliasAnalysis(design).analyze();
            new GlobalDuplicatedElimination(design).analyze();
            return new ExpandedDumper(design).dump();
        } else {
            new LocalAliasAnalysis(design).analyze();
            new LocalDuplicatedElimination(design).analyze();
            return new CompactDumper(design).dump();
        }
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
            throw new ParseCancellationException("line " + line
                    + ":" + charPositionInLine + " " + msg);
        }
    }
}
