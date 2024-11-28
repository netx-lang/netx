package pascal.hardscript;

import com.fasterxml.jackson.databind.ObjectMapper;
import pascal.hardscript.compiler.io.Builder;
import pascal.hardscript.compiler.io.Configuration;
import pascal.hardscript.hson.verilog.VerilogDumper;
import pascal.hardscript.hson.verilog.frontend.HFrontend;
import pascal.hardscript.util.print.NotepadWriter;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.Callable;

public class Main {
    public static void main(String[] args) {
        int exitCode = new CommandLine(new HardscriptApp())
                .addSubcommand("compile", new CompileCommand())
                .addSubcommand("dump", new DumpCommand())
                .execute(args);
        System.exit(exitCode);
    }

    @CommandLine.Command(
            name = "hardscript",
            description = "Main CLI for the Hardscript tool",
            mixinStandardHelpOptions = true, // Adds --help and --version options
            version = "Hardscript CLI 1.0"
    )
    static class HardscriptApp implements Callable<Integer> {
        @Override
        public Integer call() {
            System.out.println("Use subcommands like 'compile' or 'dump'.");
            return 0;
        }
    }

    @CommandLine.Command(
            name = "compile",
            description = "Compile the hardscript source code into JSON"
    )
    static class CompileCommand implements Callable<Integer> {
        @CommandLine.Parameters(
                paramLabel = "SOURCE",
                description = "The source file to compile"
        )
        private Path source;

        @CommandLine.Option(
                names = {"-c", "--compact"},
                description = "compact the output JSON",
                defaultValue = "false"
        )
        private boolean compact;

        @CommandLine.Option(
                names = {"-m", "--minimal"},
                description = "do not pretty print the output JSON",
                defaultValue = "false"
        )
        private boolean minimal;

        @CommandLine.Option(
                names = {"-t", "--top"},
                description = "name of the top component",
                defaultValue = "top"
        )
        private String top;

        @CommandLine.Option(
                names = {"-o", "--output"},
                description = "output file"
        )
        private Path output;

        @Override
        public Integer call() {
            try {
                var builder = Builder.from(Configuration.of(!compact, top));
                var node = builder.loadPath(source).compile();
                if (minimal) {
                    var mapper = new ObjectMapper();
                    if (output == null) {
                        mapper.writeValue(System.out, node);
                    } else {
                        mapper.writeValue(output.toFile(), node);
                    }
                } else {
                    builder.printer(notepadWriterFor(output)).print(node);
                }
                return 0;
            } catch (Exception e) {
                return 1;
            }
        }
    }

    @CommandLine.Command(
            name = "dump",
            description = "Dump the hardscript source code to Verilog"
    )
    static class DumpCommand implements Callable<Integer> {
        @CommandLine.Parameters(
                paramLabel = "SOURCE",
                description = "The source file to compile"
        )
        private Path source;

        @CommandLine.Option(
                names = {"-t", "--top"},
                description = "name of the top component",
                defaultValue = "top"
        )
        private String top;

        @CommandLine.Option(
                names = {"-o", "--output"},
                description = "output file"
        )
        private Path output;

        @Override
        public Integer call() {
            try {
                var builder = Builder.from(Configuration.of(false, top));
                var node = builder.loadPath(source).compile();
                new VerilogDumper(
                        notepadWriterFor(output),
                        HFrontend.load(node).parse()
                ).dump();
                return 0;
            } catch (Exception e) {
                return 1;
            }
        }
    }

    private static NotepadWriter notepadWriterFor(Path output) throws IOException {
        NotepadWriter writer;
        if (output == null) {
            writer = NotepadWriter.stdOut();
        } else {
            writer = NotepadWriter.pathOut(output);
        }
        return writer;
    }
}
