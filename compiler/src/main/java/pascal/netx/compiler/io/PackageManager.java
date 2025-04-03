package pascal.netx.compiler.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pascal.netx.compiler.ast.PackageNode;
import pascal.netx.compiler.ast.element.ComponentNode;
import pascal.netx.compiler.ast.element.ImportNode;
import pascal.netx.compiler.exception.CompileCancellationException;
import pascal.netx.compiler.model.expr.ImportRef;
import pascal.netx.compiler.model.scope.RepoScope;
import pascal.netx.util.exception.ShallNotReachHereError;
import pascal.netx.util.lang.Identifier;
import pascal.netx.util.lang.SourceLoc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class PackageManager {
    private static final Logger log = LoggerFactory.getLogger(PackageManager.class);

    private RepoScope repo;
    private final Map<Identifier, PackageNode> packs = new HashMap<>();
    private final Map<Identifier, NXCompiler> extRepos = new HashMap<>();

    public static PackageManager ofEmpty() {
        return new PackageManager();
    }

    public void setRepo(RepoScope repo) {
        if (this.repo != null) {
            throw new ShallNotReachHereError();
        }
        this.repo = repo;
    }

    public PackageNode findTopPack() {
        if (packs.size() == 1) {
            return packs.values().iterator().next();
        }
        return findApplicationPack();
    }

    private PackageNode findApplicationPack() {
        var applications = packs.values().stream().filter(PackageNode::isApplication).toList();
        if (applications.size() != 1) {
            log.error("Cannot conclude the application package of the project");
            throw new CompileCancellationException();
        }
        return applications.getFirst();
    }

    public NXCompiler dispatchCompiler(SourceLoc sourceLoc, ImportRef ref) {
        return switch (ref.sourceKind()) {
            case PACKAGE -> Objects.requireNonNull(repo.getCompiler());
            case REPO -> {
                log.info("Start to resolve repo: {}", ref.repoRef());
                yield resolveRepo(sourceLoc, ref.repoRef()).asSubCompilerOf(repo.getCompiler());
            }
        };
    }

    public List<Identifier> resolveWildcard(ImportNode node) {
        if (!node.isWildcard()) {
            throw new ShallNotReachHereError();
        }

        var reference = node.getReference();
        return switch (reference.size()) {
            case 1 -> findAllComponentsIn(resolvePackage(node.getSourceLoc(), reference.getFirst()));
            case 2 -> findAllComponentsIn(
                    resolveRepo(node.getSourceLoc(), reference.get(0))
                            .getPackManager()
                            .resolvePackage(node.getSourceLoc(), reference.get(1)));
            default -> {
                log.error("Invalid wildcard reference: {}", reference);
                throw new CompileCancellationException();
            }
        };
    }

    public List<Identifier> findAllComponentsIn(PackageNode node) {
        return node.getElements().stream()
                .filter(ComponentNode.class::isInstance)
                .map(ComponentNode.class::cast)
                .map(ComponentNode::getIdentifier)
                .toList();
    }

    public void addExternalRepo(Identifier name, NXCompiler compiler) {
        extRepos.put(name, compiler);
    }

    public NXCompiler resolveRepo(SourceLoc sourceLoc, Identifier identifier) {
        if (!extRepos.containsKey(identifier)) {
            log.error("{}: Cannot resolve repository: {}", sourceLoc, identifier);
            throw new CompileCancellationException();
        }
        return extRepos.get(identifier);
    }

    public void addPackage(PackageNode pack) {
        packs.put(pack.getPackageInfo().getIdentifier(), pack);
    }

    public PackageNode resolvePackage(SourceLoc sourceLoc, Identifier identifier) {
        if (!packs.containsKey(identifier)) {
            log.error("{}: Cannot resolve package: {}", sourceLoc, identifier);
            throw new CompileCancellationException();
        }
        return packs.get(identifier);
    }
}
