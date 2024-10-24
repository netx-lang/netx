package pascal.hardscript.compiler.ast;

import org.antlr.v4.runtime.tree.TerminalNode;

public record Identifier(String string) {
    public static Identifier of(TerminalNode string) {
        return new Identifier(string.getText());
    }
}
