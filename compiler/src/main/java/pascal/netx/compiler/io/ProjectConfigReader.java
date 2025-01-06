package pascal.netx.compiler.io;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.toml.TomlMapper;
import org.antlr.v4.runtime.CharStreams;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pascal.netx.compiler.ast.PackageNode;
import pascal.netx.compiler.ast.element.ImportNode;
import pascal.netx.compiler.ast.element.PackageElement;
import pascal.netx.compiler.exception.CompileCancellationException;
import pascal.netx.compiler.model.scope.ProjectScope;
import pascal.netx.util.lang.Identifier;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public final class ProjectConfigReader {
    private static final String DEFAULT_CONFIG = "_netx.toml";
    private static final TomlMapper MAPPER = new TomlMapper();
    private static final Logger log = LoggerFactory.getLogger(ProjectConfigReader.class);

    private final List<PackageNode> packages = new ArrayList<>();

    private final PackageManager manager = new PackageManager();
    private final Configuration config;
    private final JsonNode toml;

    private final Path dir;
    private final Path buildDir;

    private final ProjectScope.Info info;
    private NXCompiler compiler;

    public ProjectConfigReader(Configuration config, Path path) throws IOException {
        this.config = config;
        this.toml = MAPPER.readTree(new FileInputStream(path.toFile()));
        this.dir = path.getParent();
        this.buildDir = Files.createDirectories(dir.resolve("build"));

        var projectInfo = toml.get("project");
        this.info = new ProjectScope.Info(
                projectInfo.get("name").textValue(),
                ProjectScope.Version.of(projectInfo.get("version").textValue()));
    }

    public void buildCompiler() throws IOException {
        parse();
        packages.forEach(manager::addPackage);
        packages.forEach(this::resolveWildcardImportIn);
        this.compiler = new NXCompiler(info, config, manager);
    }

    public NXCompiler getCompiler() {
        return compiler;
    }

    void parse() throws IOException {
        resolveIncludePath();
        collectPackage();
        resolveRepoDependencies();
    }

    void resolveIncludePath() throws IOException {
        var include = extractSourceFiles(toml.get("project").get("include"));
        var exclude = extractSourceFiles(toml.get("project").get("exclude"));
        for (var file : include) {
            if (!exclude.contains(file)) {
                log.info("Start Parsing: {}", file);
                packages.add(new NXParser(CharStreams.fromPath(file)).parse());
            }
        }
    }

    void collectPackage() {
        var groups = packages.stream().collect(Collectors.groupingBy(PackageNode::getPackageInfo));
        packages.clear();
        for (var entry : groups.entrySet()) {
            var design = PackageNode.of(entry.getKey());
            for (var node : entry.getValue()) {
                node.getElements().forEach(design::addDefinition);
            }
            packages.add(design);
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

    void resolveRepoDependencies() throws IOException {
        if (!toml.has("dependencies")) {
            return;
        }

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
            var projConfReader = resolveDependency(path, spec);
            projConfReader.buildCompiler();
            manager.addRepoDependency(Identifier.of(entry.getKey()), projConfReader.compiler);
        }
    }

    private ProjectConfigReader resolveDependency(Path libRoot, JsonNode spec) throws IOException {
        var versionSpec = VersionSpec.of(spec.get("version").textValue());
        var libCompiler = new ProjectConfigReader(config, libRoot.resolve(DEFAULT_CONFIG));
        if (!versionSpec.isCompatibleWith(libCompiler.info.version())) {
            log.error("Dependency {} version mismatch", libCompiler.info.name());
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

    void resolveWildcardImportIn(PackageNode node) {
        var newElements = new LinkedList<PackageElement>();
        for (var element : node.getElements()) {
            if (element instanceof ImportNode importNode) {
                if (importNode.isWildcard()) {
                    var components = manager.resolveWildcard(importNode);
                    for (var id : components) {
                        var reference = new LinkedList<>(importNode.getReference()) {{ add(id); }};
                        newElements.add(ImportNode.of(reference));
                    }
                    continue;
                }
            }
            newElements.add(element);
        }
        node.setElements(newElements);
    }

    record VersionSpec(Kind kind, ProjectScope.Version base) {
        public static VersionSpec of(String spec) {
            spec = spec.replace(" ", "");
            if (spec.startsWith(">=")) {
                return new VersionSpec(Kind.GE, ProjectScope.Version.of(spec.substring(2)));
            } else if (spec.startsWith("<=")) {
                return new VersionSpec(Kind.LE, ProjectScope.Version.of(spec.substring(2)));
            } else if (spec.startsWith(">")) {
                return new VersionSpec(Kind.GT, ProjectScope.Version.of(spec.substring(1)));
            } else if (spec.startsWith("<")) {
                return new VersionSpec(Kind.LT, ProjectScope.Version.of(spec.substring(1)));
            } else {
                return new VersionSpec(Kind.EXACT, ProjectScope.Version.of(spec));
            }
        }

        boolean isCompatibleWith(ProjectScope.Version version) {
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
