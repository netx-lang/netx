package pascal.netx.compiler.analysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pascal.netx.compiler.ast.ASTListener;
import pascal.netx.compiler.ast.PackageNode;
import pascal.netx.compiler.ast.element.ComponentInstantiationNode;
import pascal.netx.compiler.ast.element.InterfaceNode;
import pascal.netx.compiler.exception.TypeCheckingException;

public final class InterfaceChecker extends ASTAnalysis implements ASTListener {
    private static final Logger log = LoggerFactory.getLogger(InterfaceChecker.class);
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
            log.error("{}: Component instantiation is not allowed inside an interface", node.getSourceLoc());
            throw new TypeCheckingException();
        }
    }

    @Override
    public void exit(InterfaceNode node) {
        insideInterface = false;
    }
}
