package pascal.netx;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import pascal.netx.backend.graphviz.DotTransformer;
import pascal.netx.backend.optimization.GraphTransformer;
import pascal.netx.backend.verilog.VerilogTransformer;
import pascal.netx.compiler.io.Configuration;
import pascal.netx.compiler.io.NXBuilder;
import pascal.netx.compiler.io.NXPrinter;
import pascal.netx.util.print.NotepadWriter;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

public class Main {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static void main(String[] args) {
        var cmd = new CommandLine(new NetXApp())
                .addSubcommand("compile", new CompileCommand())
                .addSubcommand("opt", new OptimizeCommand())
                .addSubcommand("dump", new DumpCommand());
        int exitCode = cmd.execute(args);
        System.exit(exitCode);
    }

    @FunctionalInterface
    interface CommandExecution {
        void execute() throws Exception;
    }

    @CommandLine.Command(
            name = "NetX",
            description = "Main CLI for the NetX tool",
            mixinStandardHelpOptions = true,
            usageHelpAutoWidth = true,
            version = "NetX CLI 0.1.0"
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
                description = "the source file to compile"
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
                names = {"-a", "--attrs"},
                arity = "1..*",
                description = """
                        attributes to pass to the top component
                        Examples:
                          --attrs width=32 rst=32'hDEADBEAF
                          --attrs "list = [3, 7, 21]"
                        Note: Use quotes around values containing [] or spaces"""
        )
        protected List<String> args;

        protected Configuration buildConfiguration() {
            var builder = Configuration.builder().withTop(top);
            if (args != null) {
                builder.withArgs(args);
            }
            return builder.build();
        }

        protected NotepadWriter notePad() throws IOException {
            return output == null ?
                    NotepadWriter.stdOut() :
                    NotepadWriter.pathOut(output.toAbsolutePath().normalize());
        }

        protected int handleExecution(CommandExecution execution) {
            try {
                execution.execute();
                return 0;
            } catch (Exception e) {
                System.err.println(e.getClass().getName());
                return 1;
            }
        }

        protected JsonNode getSourceNode() throws IOException {
            var path = source.toAbsolutePath().normalize();
            var fileName = path.getFileName().toString();
            var extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
            if (extension.equals("json")) {
                return OBJECT_MAPPER.readTree(Files.readString(path));
            } else {
                var builder = NXBuilder.from(buildConfiguration());
                return builder.load(path).compileAndDump();
            }
        }
    }

    @CommandLine.Command(
            name = "compile",
            usageHelpAutoWidth = true,
            description = "Compile the NetX source code into NXON representation"
    )
    static final class CompileCommand extends BaseCommand {
        @CommandLine.Option(
                names = {"-m", "--minimal"},
                description = "output NXON in a single line without formatting",
                defaultValue = "false"
        )
        private boolean minimal;

        @Override
        public Integer call() {
            return handleExecution(() -> {
                var builder = NXBuilder.from(buildConfiguration());
                var node = builder.load(source).compileAndDump();

                if (minimal) {
                    if (output == null) {
                        OBJECT_MAPPER.writeValue(System.out, node);
                    } else {
                        OBJECT_MAPPER.writeValue(output.toAbsolutePath().normalize().toFile(), node);
                    }
                } else {
                    builder.printer(notePad()).print(node);
                }
            });
        }
    }

    @CommandLine.Command(
            name = "dump",
            usageHelpAutoWidth = true,
            description = "Dump NetX source code or NXON representation into backend formats",
            subcommands = {
                    DumpCommand.VerilogCommand.class,
                    DumpCommand.DotCommand.class,
            }
    )
    static final class DumpCommand implements Callable<Integer> {
        @CommandLine.Spec
        CommandLine.Model.CommandSpec spec;

        @Override
        public Integer call() {
            spec.commandLine().usage(System.out);
            return 0;
        }

        @CommandLine.Command(
                name = "verilog",
                usageHelpAutoWidth = true,
                description = "Dump to Verilog format"
        )
        static final class VerilogCommand extends BaseCommand {
            @Override
            public Integer call() {
                return handleExecution(() -> new VerilogTransformer(notePad(), getSourceNode()).parse());
            }
        }

        @CommandLine.Command(
                name = "dot",
                usageHelpAutoWidth = true,
                description = "Dump to DOT format (Graphviz)"
        )
        static final class DotCommand extends BaseCommand {
            @CommandLine.Option(
                    names = {"-d", "--depth"},
                    description = "depth of the hierarchy to dump",
                    defaultValue = "2"
            )
            private int depth;

            @Override
            public Integer call() {
                return handleExecution(() -> new DotTransformer(notePad(), getSourceNode(), depth).parse());
            }
        }
    }

    @CommandLine.Command(
            name = "opt",
            usageHelpAutoWidth = true,
            description = "Optimize the NXON representation"
    )
    static final class OptimizeCommand extends BaseCommand {
        @CommandLine.Option(
                names = {"-m", "--minimal"},
                description = "output NXON in a single line without formatting",
                defaultValue = "false"
        )
        private boolean minimal;

        @CommandLine.Option(
                names = {"--area"},
                description = "weight for area when computing cost function",
                defaultValue = "1"
        )
        private double area;

        @CommandLine.Option(
                names = {"--delay"},
                description = "weight for delay when computing cost function",
                defaultValue = "1"
        )
        private double delay;

        @Override
        public Integer call() {
            return handleExecution(() -> {
                var transformer = new GraphTransformer(notePad(), getSourceNode());
                transformer.parse();
                transformer.rewrite();
                transformer.saturate(area, delay);

                var node = transformer.dumpNxon();
                if (minimal) {
                    if (output == null) {
                        OBJECT_MAPPER.writeValue(System.out, node);
                    } else {
                        OBJECT_MAPPER.writeValue(output.toAbsolutePath().normalize().toFile(), node);
                    }
                } else {
                    new NXPrinter(notePad()).print(node);
                }
            });
        }
    }
}