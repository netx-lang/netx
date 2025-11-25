package pascal.netx.compiler.io;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import pascal.netx.compiler.NXCompiler;
import pascal.netx.compiler.utils.Logger;
import pascal.netx.util.print.NotepadWriter;

import java.io.IOException;
import java.nio.file.Path;

public final class NXBuilder {
    private final Configuration config;

    private NXBuilder(Configuration config) {
        this.config = config;
    }

    public static NXBuilder from(Configuration config) {
        return new NXBuilder(config);
    }

    public NXCompiler load(CharStream charStream) {
        var root = new NXParser(charStream, "test.nx").parse();
        return NXCompiler.forSinglePackage(root, config);
    }

    public NXCompiler load(String string) {
        var root = new NXParser(CharStreams.fromString(string), "test.nx").parse();
        return NXCompiler.forSinglePackage(root, config);
    }

    public NXCompiler loadPath(Path path) throws IOException {
        path = path.toAbsolutePath().normalize();
        var fileName = path.getFileName().toString();
        var extension = fileName.substring(fileName.lastIndexOf('.') + 1);
        return switch (extension) {
            case "nx" -> {
                var parser = new NXParser(CharStreams.fromPath(path), fileName);
                yield NXCompiler.forSinglePackage(parser.parse(), config);
            }
            case "toml" -> {
                var projConfReader = new ProjectConfigReader(config, path);
                projConfReader.buildCompiler();
                yield projConfReader.getCompiler();
            }
            default -> {
                Logger.error("Unrecognized extension " + extension);
                throw new IOException();
            }
        };
    }

    public NXPrinter printer(NotepadWriter writer) {
        return new NXPrinter(writer);
    }
}
