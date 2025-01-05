package pascal.netx.compiler.io;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.toml.TomlMapper;
import org.antlr.v4.runtime.CharStreams;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pascal.netx.compiler.ast.DesignNode;
import pascal.netx.compiler.ast.element.ImportNode;
import pascal.netx.compiler.ast.element.PackageElement;
import pascal.netx.compiler.exception.CompileCancellationException;
import pascal.netx.util.exception.TodoException;
import pascal.netx.util.lang.Identifier;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public final class ProjectCompiler implements NXCompiler {
    private static final String DEFAULT_CONFIG = "_netx.toml";
    private static final TomlMapper MAPPER = new TomlMapper();
    private static final Logger log = LoggerFactory.getLogger(ProjectCompiler.class);

    private final List<ProjectCompiler> dependencies = new ArrayList<>();
    private final List<DesignNode> nodes = new ArrayList<>();
    private final ImportManager manager = new ImportManager();
    private final Configuration config;
    private final JsonNode toml;

    private final Path dir;
    private final Path buildDir;

    private final Info info;

    public ProjectCompiler(Configuration config, Path path) throws IOException {
        this.config = config;
        this.toml = MAPPER.readTree(new FileInputStream(path.toFile()));
        this.dir = path.getParent();
        this.buildDir = Files.createDirectories(dir.resolve("build"));

        var projectInfo = toml.get("project");
        this.info = new Info(
                projectInfo.get("name").textValue(),
                Version.of(projectInfo.get("version").textValue()));
    }

    @Override
    public JsonNode compile() throws IOException {
        parse();
        nodes.forEach(this::doImport);
        // TODO:
        throw new TodoException();
    }

    void parse() throws IOException {
        resolveIncludeDesign();
        manager.initialize(nodes, resolveDependencies());
    }

    void resolveIncludeDesign() throws IOException {
        var include = extractSourceFiles(toml.get("project").get("include"));
        var exclude = extractSourceFiles(toml.get("project").get("exclude"));
        for (var file : include) {
            if (!exclude.contains(file)) {
                log.info("Start Parsing: {}", file);
                nodes.add(new ModuleCompiler(config, CharStreams.fromPath(file)).parse());
            }
        }
    }

    LinkedHashSet<Path> extractSourceFiles(JsonNode toml) throws IOException {
        if (toml == null) {
            return new LinkedHashSet<>();
        }

        LinkedHashSet<Path> result = new LinkedHashSet<>();
        var sourceIter = toml.elements();
        while (sourceIter.hasNext()) {
            var path = dir.resolve(sourceIter.next().textValue());
            if (Files.isDirectory(path)) {
                try (var stream = Files.walk(path)) {
                    stream.filter(Files::isRegularFile).forEach(result::add);
                }
            } else {
                result.add(path);
            }
        }
        return result;
    }

    Map<Identifier, List<DesignNode>> resolveDependencies() throws IOException {
        if (!toml.has("dependencies")) {
            return Map.of();
        }

        Map<Identifier, List<DesignNode>> result = new HashMap<>();
        var iter = toml.get("dependencies").fields();
        while (iter.hasNext()) {
            var entry = iter.next();
            log.info("Start resolving dependency: {}", entry.getKey());
            var spec = entry.getValue();

            Path path;
            if (spec.has("path")) {
                path = dir.resolve(spec.get("path").textValue());
            } else if (spec.has("git")) {
                path = cloneGitRepo(spec);
            } else {
                throw new IOException("Invalid dependency specification" + entry.getKey());
            }
            var libCompiler = resolveDependency(path, spec);
            dependencies.add(libCompiler);
            libCompiler.parse();
            result.put(Identifier.of(entry.getKey()), libCompiler.nodes);
        }
        return result;
    }

    private ProjectCompiler resolveDependency(Path libRoot, JsonNode spec) throws IOException {
        var versionSpec = VersionSpec.of(spec.get("version").textValue());
        var libCompiler = new ProjectCompiler(config, libRoot.resolve(DEFAULT_CONFIG));
        if (!versionSpec.isCompatibleWith(libCompiler.info.version)) {
            log.error("Dependency {} version mismatch", libCompiler.info.name);
            throw new CompileCancellationException();
        }
        return libCompiler;
    }

    private Path cloneGitRepo(JsonNode spec) {
        try (var repo = Git.cloneRepository()
                .setURI(spec.get("git").textValue())
                .setDirectory(buildDir.toFile())
                .call()) {
            var id = repo.getRepository().getIdentifier();
            return buildDir.resolve(id);
        } catch (GitAPIException e) {
            log.error("Failed to clone git repository", e);
            throw new CompileCancellationException();
        }
    }

    void doImport(DesignNode node) {
        var newElements = new LinkedList<PackageElement>();
        for (var element : node.getElements()) {
            if (element instanceof ImportNode importNode) {
                if (importNode.isWildcard()) {
                    var prefix = importNode.getReference();
                    var components = manager.resolveWildcard(prefix);
                    for (var component : components) {
                        var reference = new LinkedList<>(prefix);
                        reference.add(component.getIdentifier());
                        newElements.add(ImportNode.of(reference));
                    }
                    continue;
                }
            }
            newElements.add(element);
        }
        node.setElements(newElements);
    }

    public record Info(String name, Version version) {

    }

    public record Version(int major, int minor, int patch) implements Comparable<Version> {
        public static Version of(String version) {
            var parts = version.split("\\.");
            return new Version(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
        }

        @Override
        public int compareTo(Version other) {
            if (major != other.major) {
                return Integer.compare(major, other.major);
            }
            if (minor != other.minor) {
                return Integer.compare(minor, other.minor);
            }
            return Integer.compare(patch, other.patch);
        }

    }

    record VersionSpec(Kind kind, Version base) {
        public static VersionSpec of(String spec) {
            spec = spec.replace(" ", "");
            if (spec.startsWith(">=")) {
                return new VersionSpec(Kind.GE, Version.of(spec.substring(2)));
            } else if (spec.startsWith("<=")) {
                return new VersionSpec(Kind.LE, Version.of(spec.substring(2)));
            } else if (spec.startsWith(">")) {
                return new VersionSpec(Kind.GT, Version.of(spec.substring(1)));
            } else if (spec.startsWith("<")) {
                return new VersionSpec(Kind.LT, Version.of(spec.substring(1)));
            } else {
                return new VersionSpec(Kind.EXACT, Version.of(spec));
            }
        }

        boolean isCompatibleWith(Version version) {
            return switch (kind) {
                case EXACT -> base.equals(version);
                case GT -> base.compareTo(version) < 0;
                case LT -> base.compareTo(version) > 0;
                case GE -> base.compareTo(version) <= 0;
                case LE -> base.compareTo(version) >= 0;
            };
        }

        enum Kind {
            EXACT, GE, GT, LE, LT
        }
    }
}
