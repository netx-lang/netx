package pascal.hardscript.compiler.ast.element;

import pascal.hardscript.compiler.ast.ASTListener;

public sealed interface PackageElement permits BindingNode, ComponentNode, ImportNode {
    void walk(ASTListener listener);
}
