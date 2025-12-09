package pascal.netx.compiler.io;

import pascal.netx.compiler.NXCompiler;
import pascal.netx.compiler.model.ast.PackageNode;
import pascal.netx.compiler.model.ast.element.ComponentNode;
import pascal.netx.compiler.model.ast.element.ImportNode;
import pascal.netx.compiler.model.circuit.expr.ImportRef;
import pascal.netx.compiler.model.circuit.scope.RepoScope;
import pascal.netx.compiler.utils.CompileCancellationException;
import pascal.netx.util.exception.ShallNotReachHereError;
import pascal.netx.util.lang.Identifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class PackageManager {
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

    public PackageNode inferTopPack() {
        if (packs.size() == 1) {
            return packs.values().iterator().next();
        }
        var applications = packs.values().stream().filter(PackageNode::isApplication).toList();
        if (applications.size() != 1) {
            Logger.error("Cannot infer the top package of the project. " +
                    "There are multiple applications: {}", applications.stream()
                    .map(PackageNode::getPackageInfo)
                    .map(info -> info.getIdentifier().toString())
                    .toList());
            throw new CompileCancellationException();
        }
        return applications.getFirst();
    }

    public NXCompiler dispatchCompiler(ImportRef ref) {
        return switch (ref.sourceKind()) {
            case PACKAGE -> Objects.requireNonNull(repo.getCompiler());
            case REPO -> resolveRepo(ref.repoRef()).asSubCompilerOf(repo.getCompiler());
        };
    }

    public List<Identifier> resolveWildcard(ImportNode node) {
        if (!node.isWildcard()) {
            throw new ShallNotReachHereError();
        }

        var reference = node.getReference();
        return switch (reference.size()) {
            case 1 -> findAllComponentsIn(resolvePackage(reference.getFirst()));
            case 2 -> findAllComponentsIn(
                    resolveRepo(reference.get(0))
                            .getPackManager()
                            .resolvePackage(reference.get(1)));
            default -> {
                Logger.error("Invalid wildcard reference: {}", reference);
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

    public NXCompiler resolveRepo(Identifier identifier) {
        if (!extRepos.containsKey(identifier)) {
            Logger.error("Cannot resolve repository: " + identifier);
            throw new CompileCancellationException();
        }
        return extRepos.get(identifier);
    }

    public void addPackage(PackageNode pack) {
        packs.put(pack.getPackageInfo().getIdentifier(), pack);
    }

    public PackageNode resolvePackage(Identifier identifier) {
        if (!packs.containsKey(identifier)) {
            Logger.error("Cannot resolve package: " + identifier);
            throw new CompileCancellationException();
        }
        return packs.get(identifier);
    }
}
