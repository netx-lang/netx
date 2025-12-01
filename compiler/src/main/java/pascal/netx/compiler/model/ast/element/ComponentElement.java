package pascal.netx.compiler.model.ast.element;

import pascal.netx.compiler.model.ast.ASTListener;
import pascal.netx.compiler.model.ast.Locatable;
import pascal.netx.util.lang.SourceLoc;

public sealed interface ComponentElement extends Locatable
        permits BindingNode, BlockNode, BranchNode, ComponentInstantiationNode, DataInstantiationNode, LoopNode {
    void walk(ASTListener listener);
    SourceLoc getSourceLoc();
}
