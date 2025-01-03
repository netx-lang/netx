package pascal.netx.compiler.ast.element;

import pascal.netx.compiler.ast.ASTListener;

public sealed interface PackageElement permits ComponentNode, ImportNode {
    void walk(ASTListener listener);
}
