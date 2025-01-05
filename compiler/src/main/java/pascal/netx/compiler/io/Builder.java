package pascal.netx.compiler.io;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import pascal.netx.util.print.NotepadWriter;

import java.io.IOException;
import java.nio.file.Path;

public final class Builder {
    private final Configuration config;

    private Builder(Configuration config) {
        this.config = config;
    }

    public static Builder forCompact() {
        return new Builder(Configuration.forCompact());
    }

    public static Builder forExpanded() {
        return new Builder(Configuration.forExpanded());
    }

    public static Builder from(Configuration config) {
        return new Builder(config);
    }

    public NXCompiler load(CharStream charStream) {
        var root = new NXParser(charStream).parse();
        return NXCompiler.forSinglePackage(root, config);
    }

    public NXCompiler load(String string) {
        var root = new NXParser(CharStreams.fromString(string)).parse();
        return NXCompiler.forSinglePackage(root, config);
    }

    public NXCompiler loadPath(Path path) throws IOException {
        var fileName = path.getFileName().toString();
        var extension = fileName.substring(fileName.lastIndexOf('.') + 1);
        return switch (extension) {
            case "nx" -> {
                var root = new NXParser(CharStreams.fromPath(path)).parse();
                yield NXCompiler.forSinglePackage(root, config);
            }
            case "toml" -> new ProjectConfigReader(config, path).build();
            default ->
                throw new IOException("Invalid file extension: " + extension);
        };
    }

    public NXPrinter printer(NotepadWriter writer) {
        return new NXPrinter(config, writer);
    }
}
