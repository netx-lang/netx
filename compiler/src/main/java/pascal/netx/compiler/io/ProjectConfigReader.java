package pascal.netx.compiler.io;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.toml.TomlMapper;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import pascal.netx.compiler.NXCompiler;
import pascal.netx.compiler.ast.PackageNode;
import pascal.netx.compiler.ast.element.ImportNode;
import pascal.netx.compiler.ast.element.PackageElement;
import pascal.netx.compiler.circuit.scope.RepoScope;
import pascal.netx.compiler.utils.CompileCancellationException;
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

    private final List<PackageNode> packages = new ArrayList<>();

    private final PackageManager manager = new PackageManager();
    private final Configuration config;
    private final JsonNode toml;

    private final Path dir;
    private final Path buildDir;

    private final RepoScope.Info info;
    private NXCompiler compiler;

    public ProjectConfigReader(Configuration config, Path path) throws IOException {
        this.config = config;
        this.toml = MAPPER.readTree(new FileInputStream(path.toFile()));
        this.dir = path.getParent();
        this.buildDir = dir.resolve("build");

        var projectInfo = toml.get("project");
        this.info = new RepoScope.Info(
                projectInfo.get("name").textValue(),
                RepoScope.Version.of(projectInfo.get("version").textValue()));
    }

    public void buildCompiler() throws IOException {
        parse();
        packages.forEach(manager::addPackage);
        packages.forEach(this::resolveWildcardImportIn);
        this.compiler = new NXCompiler(config, manager);
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
                Logger.info("Start Parsing: {}", file);
                var parser = new NXParser(file);
                packages.add(parser.parse());
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
                    stream.filter(Files::isRegularFile)
                            .filter(ProjectConfigReader::isNetXFile)
                            .forEach(result::add);
                }
            } else {
                result.add(path);
            }
        }
        return result;
    }

    private static boolean isNetXFile(Path file) {
        return file.getFileName().toString().endsWith(".nx");
    }

    void resolveRepoDependencies() throws IOException {
        if (!toml.has("dependencies")) {
            return;
        }

        var iter = toml.get("dependencies").fields();
        while (iter.hasNext()) {
            var entry = iter.next();
            Logger.info("Start resolving dependency: {}", entry.getKey());
            var spec = entry.getValue();

            Path path;
            if (spec.has("path")) {
                path = dir.resolve(spec.get("path").textValue());
            } else if (spec.has("git")) {
                path = cloneGitRepo(entry.getKey(), spec);
            } else {
                throw new IOException("Invalid dependency specification" + entry.getKey());
            }
            var projConfReader = resolveDependency(path, spec);
            projConfReader.buildCompiler();
            manager.addExternalRepo(Identifier.of(entry.getKey()), projConfReader.compiler);
        }
    }

    private ProjectConfigReader resolveDependency(Path libRoot, JsonNode spec) throws IOException {
        var libCompiler = new ProjectConfigReader(config, libRoot.resolve(DEFAULT_CONFIG));
        if (spec.has("version")) {
            var versionSpec = VersionSpec.of(spec.get("version").textValue());
            if (!versionSpec.isCompatibleWith(libCompiler.info.version())) {
                Logger.error("Dependency {} version mismatch: expected {}, got {}",
                        libCompiler.info.name(), versionSpec.base, libCompiler.info.version());
                throw new CompileCancellationException();
            }
        }
        return libCompiler;
    }

    private static void checkoutGitRepo(JsonNode spec, Git repo) throws GitAPIException {
        String checkout = null;
        if (spec.has("commit")) {
            checkout = spec.get("commit").textValue();
        } else if (spec.has("branch")) {
            checkout = spec.get("branch").textValue();
        } else if (spec.has("tag")) {
            checkout = spec.get("tag").textValue();
        }

        if (checkout != null) {
            repo.checkout().setName(checkout).call();
        }
    }

    private Path cloneGitRepo(String name, JsonNode spec) throws IOException {
        Git repo;
        Path repoDir = buildDir.resolve(name);
        try {
            if (Files.exists(repoDir)) {
                repo = Git.open(repoDir.toFile());
                try {
                    checkoutGitRepo(spec, repo);
                } catch (GitAPIException e) {
                    repo.fetch().call();
                }
            } else {
                repo = Git.cloneRepository()
                        .setURI(spec.get("git").textValue())
                        .setDirectory(repoDir.toFile())
                        .call();
            }
            checkoutGitRepo(spec, repo);
            repo.close();
        } catch (GitAPIException e) {
            Logger.error("Failed to open git repository: {}", e.getMessage());
            throw new CompileCancellationException();
        }
        return repoDir;
    }

    void resolveWildcardImportIn(PackageNode node) {
        var newElements = new LinkedList<PackageElement>();
        for (var element : node.getElements()) {
            if (element instanceof ImportNode importNode) {
                if (importNode.isWildcard()) {
                    for (var id : manager.resolveWildcard(importNode)) {
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

    record VersionSpec(Kind kind, RepoScope.Version base) {
        public static VersionSpec of(String spec) {
            spec = spec.replace(" ", "");
            if (spec.startsWith(">=")) {
                return new VersionSpec(Kind.GE, RepoScope.Version.of(spec.substring(2)));
            } else if (spec.startsWith("<=")) {
                return new VersionSpec(Kind.LE, RepoScope.Version.of(spec.substring(2)));
            } else if (spec.startsWith(">")) {
                return new VersionSpec(Kind.GT, RepoScope.Version.of(spec.substring(1)));
            } else if (spec.startsWith("<")) {
                return new VersionSpec(Kind.LT, RepoScope.Version.of(spec.substring(1)));
            } else {
                return new VersionSpec(Kind.EXACT, RepoScope.Version.of(spec));
            }
        }

        boolean isCompatibleWith(RepoScope.Version version) {
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
