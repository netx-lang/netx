package pascal.netx;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pascal.netx.compiler.io.Configuration;
import pascal.netx.compiler.io.NXBuilder;
import pascal.netx.util.print.NotepadWriter;
import pascal.netx.verilog.VerilogDumper;
import pascal.netx.verilog.frontend.HFrontend;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        int exitCode = new CommandLine(new NetXApp())
                .addSubcommand("compile", new CompileCommand())
                .addSubcommand("dump", new DumpCommand())
                .addSubcommand("visualize", new VisualizeCommand())
                .execute(args);
        System.exit(exitCode);
    }

    @CommandLine.Command(
            name = "NetX",
            description = "Main CLI for the NetX tool",
            mixinStandardHelpOptions = true, // Adds --help and --version options
            version = "NetX CLI 1.0"
    )
    static class NetXApp implements Callable<Integer> {
        @CommandLine.Spec
        CommandLine.Model.CommandSpec spec;

        @Override
        public Integer call() {
            spec.commandLine().usage(System.out);
            return 0;
        }
    }

    static abstract class BaseCommand implements Callable<Integer> {
        @CommandLine.Parameters(
                paramLabel = "SOURCE",
                description = "The source file to compile"
        )
        protected Path source;

        @CommandLine.Option(
                names = {"-t", "--top"},
                description = "name of the top component",
                defaultValue = "top"
        )
        protected String top;

        @CommandLine.Option(
                names = {"-o", "--output"},
                description = "output file"
        )
        protected Path output;

        @CommandLine.Option(
                names = {"-a", "--args"},
                arity = "1..*",
                description = "arguments to pass to the top component, " +
                        "in the form of --args a=true b=2 c=2'b01"
        )
        protected List<String> args;

        protected Configuration.Builder setupConfiguration() {
            var builder = Configuration.builder();
            builder.withTop(top);
            if (args != null) {
                builder.withArgs(args);
            }
            return builder;
        }

        protected NotepadWriter notepadWriterFor(Path output) throws IOException {
            NotepadWriter writer;
            if (output == null) {
                writer = NotepadWriter.stdOut();
            } else {
                writer = NotepadWriter.pathOut(output.toAbsolutePath().normalize());
            }
            return writer;
        }
    }

    @CommandLine.Command(
            name = "compile",
            description = "Compile the NetX source code into JSON"
    )
    static class CompileCommand extends BaseCommand {
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

        @Override
        public Integer call() {
            try {
                var builder = NXBuilder.from(buildConfig());
                var node = builder.loadPath(source).compileAndDump();
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
                log.error("Compilation failed", e);
                return 1;
            }
        }

        private Configuration buildConfig() {
            var builder = super.setupConfiguration();
            if (compact) {
                builder.forCompact();
            } else {
                builder.forExpanded();
            }
            return builder.build();
        }
    }

    @CommandLine.Command(
            name = "dump",
            description = "Dump the NetX source code to Verilog"
    )
    static class DumpCommand extends BaseCommand {
        @Override
        public Integer call() {
            try {
                var builder = NXBuilder.from(setupConfiguration().forCompact().build());
                var node = builder.loadPath(source).compileAndDump();
                new VerilogDumper(
                        notepadWriterFor(output),
                        HFrontend.load(node).parse()
                ).dump();
                return 0;
            } catch (Exception e) {
                log.error("Dumping failed", e);
                return 1;
            }
        }
    }

    @CommandLine.Command(
            name = "visualize",
            description = "Visualize the NetX source code"
    )
    static class VisualizeCommand extends BaseCommand {
        @CommandLine.Option(
                names = {"-d", "--depth"},
                description = "depth of visualization",
                defaultValue = "1"
        )
        private int depth;

        @Override
        public Integer call() {
            try {
                var builder = NXBuilder.from(setupConfiguration().forCompact().build());
                var compiler = builder.loadPath(source);
                compiler.compileAndVisualize(notepadWriterFor(output), depth);
                return 0;
            } catch (Exception e) {
                log.error("Visualization failed", e);
                return 1;
            }
        }
    }
}