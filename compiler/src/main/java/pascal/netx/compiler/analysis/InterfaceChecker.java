package pascal.netx.compiler.analysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pascal.netx.compiler.ast.ASTListener;
import pascal.netx.compiler.ast.PackageNode;
import pascal.netx.compiler.ast.element.ComponentInstantiationNode;
import pascal.netx.compiler.ast.element.ComponentNode;
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
    public void enter(ComponentNode node) {
        if (node.getKind() == ComponentNode.Kind.INTERFACE) {
            insideInterface = true;
            if (!node.getAttributes().isEmpty()) {
                log.error("{}: Interface cannot have attributes", node.getSourceLoc());
                throw new TypeCheckingException();
            }
        }
    }

    @Override
    public void enter(ComponentInstantiationNode node) {
        if (insideInterface) {
            log.error("{}: Component instantiation is not allowed inside an interface", node.getSourceLoc());
            throw new TypeCheckingException();
        }
    }

    @Override
    public void exit(ComponentNode node) {
        if (node.getKind() == ComponentNode.Kind.INTERFACE) {
            insideInterface = false;
        }
    }
}
