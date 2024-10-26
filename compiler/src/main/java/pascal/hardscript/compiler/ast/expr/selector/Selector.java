package pascal.hardscript.compiler.ast.expr.selector;

import pascal.hardscript.compiler.ast.ASTNode;

public abstract sealed class Selector extends ASTNode permits BitSelector, PartSelector {
}
