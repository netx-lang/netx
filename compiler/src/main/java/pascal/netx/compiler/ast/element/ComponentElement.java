package pascal.netx.compiler.ast.element;

import pascal.netx.compiler.ast.ASTListener;

public sealed interface ComponentElement
        permits BindingNode, ComponentInstantiationNode, DataInstantiationNode, LoopNode {
    void walk(ASTListener listener);
}
