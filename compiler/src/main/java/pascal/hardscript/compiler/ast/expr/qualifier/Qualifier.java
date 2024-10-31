package pascal.hardscript.compiler.ast.expr.qualifier;

import pascal.hardscript.compiler.ast.ASTNode;

public abstract sealed class Qualifier extends ASTNode permits BindingQualifier, FilterQualifier, GeneratorQualifier {
}
