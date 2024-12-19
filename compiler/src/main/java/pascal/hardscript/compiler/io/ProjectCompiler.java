package pascal.hardscript.compiler.io;

import com.fasterxml.jackson.databind.JsonNode;
import org.antlr.v4.runtime.CharStreams;
import pascal.hardscript.compiler.ast.DesignNode;
import pascal.hardscript.compiler.ast.element.ImportNode;
import pascal.hardscript.util.exception.TodoException;
import pascal.hardscript.util.lang.Identifier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public final class ProjectCompiler {
    private static final Path DIR = Path.of(System.getProperty("user.dir"));

    private final List<DesignNode> nodes = new ArrayList<>();
    private final ImportManager manager = new ImportManager();
    private final Configuration config;
    private final JsonNode toml;

    public ProjectCompiler(Configuration config, JsonNode toml) {
        this.config = config;
        this.toml = toml;
    }

    public JsonNode compile() throws IOException {
        resolveIncludeDesign();
        manager.initialize(nodes, resolveDependencies());
        nodes.forEach(this::doImport);
        return null;
    }

    void resolveIncludeDesign() throws IOException {
        var include = extractSourceFiles(toml.get("project").get("include"));
        var exclude = extractSourceFiles(toml.get("project").get("exclude"));
        for (var file : include) {
            if (!exclude.contains(file)) {
                nodes.add(new HSCompiler(config, CharStreams.fromPath(file)).parse());
            }
        }
    }

    LinkedHashSet<Path> extractSourceFiles(JsonNode toml) throws IOException {
        LinkedHashSet<Path> result = new LinkedHashSet<>();
        var sourceIter = toml.elements();
        while (sourceIter.hasNext()) {
            var path = DIR.resolve(sourceIter.next().textValue());
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
        var iter = toml.get("dependencies").fields();
        while (iter.hasNext()) {
            var entry = iter.next();
            var spec = entry.getValue();
            if (spec.has("path")) {
                // TODO: implement local dependency
                throw new TodoException();
            } else if (spec.has("git")) {
                // TODO: implement git dependency
                throw new TodoException();
            } else {
                throw new IOException("Invalid dependency specification" + entry.getKey());
            }
        }
        return null;
    }

    void doImport(DesignNode node) {
        for (var element : node.getElements()) {
            if (element instanceof ImportNode importNode) {

            }
        }
    }
}
