package pascal.hardscript.compiler.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pascal.hardscript.compiler.ast.DesignNode;
import pascal.hardscript.compiler.ast.PackageInfoNode;
import pascal.hardscript.compiler.ast.element.ComponentNode;
import pascal.hardscript.compiler.exception.CompileCancellationException;
import pascal.hardscript.util.lang.Identifier;

import java.util.*;

public final class ImportManager {
    private static final Logger log = LoggerFactory.getLogger(ImportManager.class);
    private final LibraryMap projectMap = new LibraryMap();
    private final RepoMap dependencyMap = new RepoMap();

    public void initialize(List<DesignNode> project, Map<Identifier, List<DesignNode>> dependencies) {
        buildLibMapForRepo(project, projectMap);
        for (var repo : dependencies.entrySet()) {
            var libMap = new LibraryMap();
            buildLibMapForRepo(repo.getValue(), libMap);
            dependencyMap.put(repo.getKey(), libMap);
        }
    }

    public ComponentNode resolve(List<Identifier> reference) {
        return switch (reference.size()) {
            case 2 -> {
                var library = resolveLibrary(reference);
                if (!library.containsKey(reference.get(1))) {
                    log.error("Component {} not found in library {}",
                            reference.get(1), reference.getFirst());
                    throw new CompileCancellationException();
                }
                yield library.get(reference.get(1));
            }
            case 3 -> {
                var repo = resolveRepo(reference);
                if (!repo.containsKey(reference.get(1))) {
                    log.error("Library {} not found in dependent repo {}",
                            reference.get(1), reference.getFirst());
                    throw new CompileCancellationException();
                }
                var library = repo.get(reference.get(1));
                if (!library.containsKey(reference.get(2))) {
                    log.error("Component {} not found in library {} in repo {}",
                            reference.get(2), reference.get(1), reference.getFirst());
                    throw new CompileCancellationException();
                }
                yield library.get(reference.get(2));
            }
            default -> {
                log.error("Invalid reference: {}", reference);
                throw new CompileCancellationException();
            }
        };
    }

    public Collection<ComponentNode> resolveWildcard(List<Identifier> reference) {
        return switch (reference.size()) {
            case 1 -> resolveLibrary(reference).values();
            case 2 -> {
                var repo = resolveRepo(reference);
                if (!repo.containsKey(reference.get(1))) {
                    log.error("Library {} not found in repo {}",
                            reference.get(1), reference.getFirst());
                    throw new CompileCancellationException();
                }
                yield repo.get(reference.get(1)).values();
            }
            default -> {
                log.error("Invalid wildcard reference: {}", reference);
                throw new CompileCancellationException();
            }
        };
    }

    private LibraryMap resolveRepo(List<Identifier> reference) {
        if (!dependencyMap.containsKey(reference.getFirst())) {
            log.error("Dependent repo {} not found", reference);
            throw new CompileCancellationException();
        }
        return dependencyMap.get(reference.getFirst());
    }

    private Map<Identifier, ComponentNode> resolveLibrary(List<Identifier> reference) {
        if (!projectMap.containsKey(reference.getFirst())) {
            log.error("Library {} not found", reference.getFirst());
            throw new CompileCancellationException();
        }
        return projectMap.get(reference.getFirst());
    }

    private void buildLibMapForRepo(List<DesignNode> project, LibraryMap map) {
        for (var definition : project) {
            var packageInfo = definition.getPackageInfo();
            if (packageInfo.getKind() == PackageInfoNode.Kind.LIBRARY) {
                var id = packageInfo.getIdentifier();
                if (!map.containsKey(id)) {
                    map.put(id, new LinkedHashMap<>());
                }
                for (var element : definition.getElements()) {
                    if (element instanceof ComponentNode component) {
                        map.get(id).put(component.getIdentifier(), component);
                    }
                }
            }
        }
    }

    static class LibraryMap extends HashMap<Identifier, Map<Identifier, ComponentNode>> {}

    static class RepoMap extends HashMap<Identifier, LibraryMap> {}
}
