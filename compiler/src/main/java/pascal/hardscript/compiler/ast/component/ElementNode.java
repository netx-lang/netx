package pascal.hardscript.compiler.ast.component;

import pascal.hardscript.compiler.ast.ASTNode;

public abstract sealed class ElementNode extends ASTNode permits BindingNode, ComponentInstantiationNode, DataInstantiationNode, LoopNode {
}
