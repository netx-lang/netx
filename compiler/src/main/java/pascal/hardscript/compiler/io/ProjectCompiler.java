package pascal.hardscript.compiler.io;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.toml.TomlMapper;
import org.antlr.v4.runtime.CharStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pascal.hardscript.compiler.ast.DesignNode;
import pascal.hardscript.compiler.ast.element.ImportNode;
import pascal.hardscript.compiler.ast.element.PackageElement;
import pascal.hardscript.util.exception.TodoException;
import pascal.hardscript.util.lang.Identifier;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public final class ProjectCompiler implements Compiler {
    private static final String DEFAULT_CONFIG = "hardpack.toml";
    private static final TomlMapper MAPPER = new TomlMapper();
    private static final Logger log = LoggerFactory.getLogger(ProjectCompiler.class);

    private final List<ProjectCompiler> dependencies = new ArrayList<>();
    private final List<DesignNode> nodes = new ArrayList<>();
    private final ImportManager manager = new ImportManager();
    private final Configuration config;
    private final JsonNode toml;
    private final Path dir;

    public ProjectCompiler(Configuration config, Path path) throws IOException {
        this.config = config;
        this.toml = MAPPER.readTree(new FileInputStream(path.toFile()));
        this.dir = path.getParent();
    }

    public void parse() throws IOException {
        resolveIncludeDesign();
        manager.initialize(nodes, resolveDependencies());
    }

    @Override
    public JsonNode compile() throws IOException {
        parse();
        nodes.forEach(this::doImport);
        // TODO:
        throw new TodoException();
    }

    void resolveIncludeDesign() throws IOException {
        var include = extractSourceFiles(toml.get("project").get("include"));
        var exclude = extractSourceFiles(toml.get("project").get("exclude"));
        for (var file : include) {
            if (!exclude.contains(file)) {
                log.info("Start Parsing: {}", file);
                nodes.add(new HSCompiler(config, CharStreams.fromPath(file)).parse());
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
            if (spec.has("path")) {
                var libRoot = dir.resolve(spec.get("path").textValue());
                var libCompiler = new ProjectCompiler(config, libRoot.resolve(DEFAULT_CONFIG));
                libCompiler.parse();
                dependencies.add(libCompiler);
                result.put(Identifier.of(entry.getKey()), libCompiler.nodes);
            } else if (spec.has("git")) {
                // TODO: implement git dependency
                throw new TodoException();
            } else {
                throw new IOException("Invalid dependency specification" + entry.getKey());
            }
        }
        return result;
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
}
