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

    public ModuleCompiler load(CharStream charStream) {
        return new ModuleCompiler(config, charStream);
    }

    public ModuleCompiler load(String string) {
        return new ModuleCompiler(config, CharStreams.fromString(string));
    }

    public NXCompiler loadPath(Path path) throws IOException {
        var fileName = path.getFileName().toString();
        var extension = fileName.substring(fileName.lastIndexOf('.') + 1);
        return switch (extension) {
            case "nx" -> new ModuleCompiler(config, CharStreams.fromPath(path));
            case "toml" -> new ProjectCompiler(config, path);
            default ->
                throw new IOException("Invalid file extension: " + extension);
        };
    }

    public NXPrinter printer(NotepadWriter writer) {
        return new NXPrinter(config, writer);
    }
}
