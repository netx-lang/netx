package pascal.netx.compiler.analysis.ast;

import pascal.netx.compiler.ast.ASTListener;
import pascal.netx.compiler.ast.PackageNode;
import pascal.netx.compiler.ast.element.ComponentInstantiationNode;
import pascal.netx.compiler.ast.element.InterfaceNode;
import pascal.netx.compiler.utils.Logger;
import pascal.netx.compiler.utils.TypeCheckingException;

/**
 * Ensure interfaces do not contain component instantiations.
 */
public final class InterfaceChecker extends ASTAnalysis implements ASTListener {
    private boolean insideInterface = false;

    public InterfaceChecker(PackageNode design) {
        super(design);
    }

    @Override
    public void analyze() {
        design.walk(this);
    }

    @Override
    public void enter(InterfaceNode node) {
        insideInterface = true;
    }

    @Override
    public void enter(ComponentInstantiationNode node) {
        if (insideInterface) {
            Logger.error(node.getSourceLoc(), "Component instantiation is not allowed inside an interface");
            throw new TypeCheckingException();
        }
    }

    @Override
    public void exit(InterfaceNode node) {
        insideInterface = false;
    }
}
