package pascal.netx.compiler.model.ast.element;

import pascal.netx.compiler.model.ast.ASTListener;
import pascal.netx.compiler.model.ast.Locatable;

public sealed interface PackageElement extends Locatable permits ComponentNode, EnumNode, ImportNode, InterfaceNode {
    void walk(ASTListener listener);
}
