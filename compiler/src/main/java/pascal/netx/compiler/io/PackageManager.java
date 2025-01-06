package pascal.netx.compiler.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pascal.netx.compiler.ast.PackageNode;
import pascal.netx.compiler.ast.element.ComponentNode;
import pascal.netx.compiler.ast.element.ImportNode;
import pascal.netx.compiler.exception.CompileCancellationException;
import pascal.netx.compiler.model.scope.ProjectScope;
import pascal.netx.util.exception.ShallNotReachHereError;
import pascal.netx.util.lang.Identifier;
import pascal.netx.util.lang.SourceLoc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class PackageManager {
    private static final Logger log = LoggerFactory.getLogger(PackageManager.class);

    private final Map<Identifier, NXCompiler> repositories = new HashMap<>();
    private final Map<Identifier, PackageNode> packages = new HashMap<>();
    private ProjectScope project;

    public static PackageManager ofEmpty() {
        return new PackageManager();
    }

    public void addRepoDependency(Identifier name, NXCompiler compiler) {
        repositories.put(name, compiler);
    }

    public void addPackage(PackageNode pack) {
        packages.put(pack.getPackageInfo().getIdentifier(), pack);
    }

    public void setProject(ProjectScope project) {
        if (this.project != null) {
            throw new ShallNotReachHereError();
        }
        this.project = project;
    }

    public NXCompiler dispatchCompiler(SourceLoc sourceLoc, List<Identifier> reference) {
       return switch (reference.size()) {
            case 2 -> Objects.requireNonNull(project.getCompiler());
            case 3 -> {
                var result = resolveRepo(sourceLoc, reference.getFirst());
                result.setDataIndexer(project.getCompiler().getDataIndexer());
                yield result;
            }
            default -> {
                log.error("{}: Invalid reference in import: {}", sourceLoc, reference);
                throw new CompileCancellationException();
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

    public PackageNode resolvePackage(SourceLoc sourceLoc, Identifier identifier) {
        if (!packages.containsKey(identifier)) {
            log.error("{}: Cannot resolve package: {}", sourceLoc, identifier);
            throw new CompileCancellationException();
        }
        return packages.get(identifier);
    }

    public NXCompiler resolveRepo(SourceLoc sourceLoc, Identifier identifier) {
        if (!repositories.containsKey(identifier)) {
            log.error("{}: Cannot resolve repository: {}", sourceLoc, identifier);
            throw new CompileCancellationException();
        }
        return repositories.get(identifier);
    }

    public PackageNode findApplicationPack() {
        var applications = packages.values().stream().filter(PackageNode::isApplication).toList();
        if (applications.size() != 1) {
            // FIXME:
            log.error("Cannot conclude the application package of the project");
            throw new CompileCancellationException();
        }
        return applications.getFirst();
    }
}
