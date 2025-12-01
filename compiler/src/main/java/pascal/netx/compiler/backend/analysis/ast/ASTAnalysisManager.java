package pascal.netx.compiler.backend.analysis.ast;

import pascal.netx.compiler.io.Logger;
import pascal.netx.compiler.model.ast.PackageNode;
import pascal.netx.util.exception.ShallNotReachHereError;

/**
 * Manages the execution of AST analyses for a specific package.
 * This class is responsible for instantiating and running analyses defined by subclasses of ASTAnalysis.
 */
public final class ASTAnalysisManager {
    private final PackageNode node;

    public ASTAnalysisManager(PackageNode node) {
        this.node = node;
    }

    public void analyze(Class<? extends ASTAnalysis> type) {
        try {
            var method = type.getConstructor(PackageNode.class);
            method.newInstance(node).analyze();
        } catch (ReflectiveOperationException e) {
            Logger.error("Failed to register analysis: {}", type.getName());
            throw new ShallNotReachHereError();
        }
    }
}
