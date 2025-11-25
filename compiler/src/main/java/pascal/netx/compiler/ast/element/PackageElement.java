package pascal.netx.compiler.ast.element;

import pascal.netx.compiler.ast.ASTListener;
import pascal.netx.compiler.ast.Locatable;

public sealed interface PackageElement extends Locatable permits ComponentNode, EnumNode, ImportNode, InterfaceNode {
    void walk(ASTListener listener);
}
