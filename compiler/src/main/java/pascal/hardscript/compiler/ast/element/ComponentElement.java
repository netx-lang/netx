package pascal.hardscript.compiler.ast.element;

import pascal.hardscript.compiler.ast.ASTListener;

public sealed interface ComponentElement
        permits BindingNode, ComponentInstantiationNode, DataInstantiationNode, LoopNode {
    void walk(ASTListener listener);
}
