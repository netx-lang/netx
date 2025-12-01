package pascal.netx.compiler.model.ast.expr.selector;

import pascal.netx.compiler.model.ast.ASTNode;

public abstract sealed class Selector extends ASTNode permits BitSelector, PartSelector {
}
