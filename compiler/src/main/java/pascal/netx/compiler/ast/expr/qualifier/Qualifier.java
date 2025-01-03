package pascal.netx.compiler.ast.expr.qualifier;

import pascal.netx.compiler.ast.ASTNode;

public abstract sealed class Qualifier extends ASTNode permits BindingQualifier, FilterQualifier, GeneratorQualifier {
}
