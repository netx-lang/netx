package pascal.hardscript.compiler.io;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import pascal.hardscript.util.print.NotepadWriter;

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

    public HSCompiler load(CharStream charStream) {
        return new HSCompiler(config, charStream);
    }

    public HSCompiler load(String string) {
        return new HSCompiler(config, CharStreams.fromString(string));
    }

    public HSCompiler loadPath(Path path) throws IOException {
        return new HSCompiler(config, CharStreams.fromPath(path));
    }

    public HSPrinter printer(NotepadWriter writer) {
        return new HSPrinter(config, writer);
    }
}
