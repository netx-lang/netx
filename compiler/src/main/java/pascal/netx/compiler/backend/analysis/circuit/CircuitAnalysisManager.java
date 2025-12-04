package pascal.netx.compiler.backend.analysis.circuit;

import pascal.netx.compiler.io.Logger;
import pascal.netx.compiler.model.circuit.scope.PackageScope;
import pascal.netx.compiler.model.circuit.scope.RepoScope;
import pascal.netx.util.exception.ShallNotReachHereError;

/**
 * Manages the execution of model analyses across all packages in a repository.
 * This class is responsible for instantiating and running analyses defined by subclasses of ModelAnalysis.
 */
public final class CircuitAnalysisManager {
    private final RepoScope repository;

    public CircuitAnalysisManager(RepoScope repository) {
        this.repository = repository;
    }

    public void analyze(Class<? extends CircuitAnalysis> type) {
        try {
            var method = type.getConstructor(PackageScope.class);
            for (var child : repository.packages()) {
                method.newInstance(child).analyze();
            }
        } catch (ReflectiveOperationException e) {
            Logger.error("Failed to register analysis: {}", type.getName());
            throw new ShallNotReachHereError();
        }
    }
}
