package pascal.netx.compiler.ast.element;

import pascal.netx.compiler.ast.ASTListener;

public sealed interface PackageElement permits ComponentNode, EnumNode, ImportNode, InterfaceNode {
    void walk(ASTListener listener);
}
