package pascal.hardscript.compiler.ast;

import org.antlr.v4.runtime.Token;

public record SourceLoc(int line, int column) {

    public static SourceLoc of(int line, int column) {
        return new SourceLoc(line, column);
    }

    public static SourceLoc from(Token token) {
        return new SourceLoc(token.getLine(), token.getCharPositionInLine());
    }

    public String toString() {
        return line + ":" + column;
    }
}
