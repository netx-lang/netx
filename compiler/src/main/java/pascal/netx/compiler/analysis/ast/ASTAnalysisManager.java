package pascal.netx.compiler.analysis.ast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pascal.netx.compiler.ast.PackageNode;
import pascal.netx.compiler.exception.CompileCancellationException;

/**
 * Manages the execution of AST analyses for a specific package.
 * This class is responsible for instantiating and running analyses defined by subclasses of ASTAnalysis.
 */
public final class ASTAnalysisManager {
    private static final Logger log = LoggerFactory.getLogger(ASTAnalysisManager.class);
    private final PackageNode node;

    public ASTAnalysisManager(PackageNode node) {
        this.node = node;
    }

    public void analyze(Class<? extends ASTAnalysis> type) {
        try {
            var method = type.getConstructor(PackageNode.class);
            method.newInstance(node).analyze();
        } catch (ReflectiveOperationException e) {
            log.error("Failed to register analysis: {}", type.getName());
            throw new CompileCancellationException();
        }
    }
}
