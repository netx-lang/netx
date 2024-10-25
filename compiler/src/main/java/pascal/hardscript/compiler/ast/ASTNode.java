package pascal.hardscript.compiler.ast;

public interface ASTNode {
    void walk(ASTListener listener);
}
