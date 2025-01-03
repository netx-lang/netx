package pascal.netx.compiler.ast.expr.selector;

import pascal.netx.compiler.ast.ASTNode;

public abstract sealed class Selector extends ASTNode permits BitSelector, PartSelector {
}
