package pascal.hardscript;

import pascal.hardscript.compiler.io.Builder;
import pascal.hardscript.compiler.io.Configuration;
import pascal.hardscript.util.print.NotepadWriter;

import java.io.IOException;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) throws IOException {
        var builder = Builder.from(Configuration.of(args[1].equals("0"), args[2]));
        var node = builder.loadPath(Path.of(args[0])).compile();
        NotepadWriter writer;
        if (args[3].equals("-")) {
            writer = NotepadWriter.stdOut();
        } else {
            writer = NotepadWriter.pathOut(Path.of(args[3]));
        }
        builder.printer(writer).print(node);
    }
}