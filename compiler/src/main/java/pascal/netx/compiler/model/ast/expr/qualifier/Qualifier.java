package pascal.netx.compiler.model.ast.expr.qualifier;

import pascal.netx.compiler.model.ast.ASTNode;

public abstract sealed class Qualifier extends ASTNode permits BindingQualifier, FilterQualifier, GeneratorQualifier {
}
