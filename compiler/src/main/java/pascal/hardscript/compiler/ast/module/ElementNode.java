package pascal.hardscript.compiler.ast.module;

import pascal.hardscript.compiler.ast.ASTNode;

public abstract sealed class ElementNode extends ASTNode permits ComponentNode, InstantiationNode, LoopNode {
}
