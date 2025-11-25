package pascal.netx.compiler.ast.element;

import pascal.netx.compiler.ast.ASTListener;
import pascal.netx.compiler.ast.Locatable;
import pascal.netx.util.lang.SourceLoc;

public sealed interface ComponentElement extends Locatable
        permits BindingNode, BlockNode, BranchNode, ComponentInstantiationNode, DataInstantiationNode, LoopNode {
    void walk(ASTListener listener);
    SourceLoc getSourceLoc();
}
