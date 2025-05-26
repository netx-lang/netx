package pascal.netx.compiler.analysis.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pascal.netx.compiler.exception.CompileCancellationException;
import pascal.netx.compiler.model.scope.PackageScope;
import pascal.netx.compiler.model.scope.RepoScope;


public final class ModelAnalysisManager {
    private static final Logger log = LoggerFactory.getLogger(ModelAnalysisManager.class);
    private final RepoScope repository;

    public ModelAnalysisManager(RepoScope repository) {
        this.repository = repository;
    }

    public void analyze(Class<? extends ModelAnalysis> type) {
        try {
            var method = type.getConstructor(PackageScope.class);
            for (var child : repository.packages()) {
                method.newInstance(child).analyze();
            }
        } catch (ReflectiveOperationException e) {
            log.error("Failed to register analysis: {}", type.getName());
            throw new CompileCancellationException();
        }
    }
}
