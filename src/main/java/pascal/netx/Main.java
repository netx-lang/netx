package pascal.netx;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pascal.netx.compiler.io.Builder;
import pascal.netx.compiler.io.Configuration;
import pascal.netx.compiler.model.scope.binding.MultiBinding;
import pascal.netx.compiler.model.variable.BitVectorValue;
import pascal.netx.compiler.model.variable.BooleanValue;
import pascal.netx.compiler.model.variable.Int32Value;
import pascal.netx.compiler.model.variable.Value;
import pascal.netx.util.lang.BitVector;
import pascal.netx.util.lang.Identifier;
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

    @CommandLine.Command(
            name = "compile",
            description = "Compile the NetX source code into JSON"
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

        @CommandLine.Option(
                names = {"-a", "--args"},
                arity = "1..*",
                description = "arguments to pass to the top component, " +
                        "in the form of --args a=true b=2 c=2'b01"
        )
        private List<String> args;

        @Override
        public Integer call() {
            try {
                var builder = Builder.from(setupConfiguration());
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
                return 1;
            }
        }

        private Configuration setupConfiguration() {
            Configuration config;
            if (compact) {
                config = Configuration.forCompact();
            } else {
                config = Configuration.forExpanded();
            }
            config = config.withTop(top);
            if (args != null) {
                config = config.withArgs(parseArgBindings());
            }
            return config;
        }

        private MultiBinding parseArgBindings() {
            var bindings = MultiBinding.ofEmpty();
            for (String arg : args) {
                String[] parts = arg.split("=");
                if (parts.length == 2) {
                    bindings.bind(Identifier.of(parts[0]), parseValue(parts[1]));
                } else {
                    log.error("Invalid argument format: {}", arg);
                    throw new IllegalArgumentException("Invalid argument format: " + arg);
                }
            }
            return bindings;
        }

        private Value parseValue(String value) {
            if ("true".equals(value) || "false".equals(value)) {
                return BooleanValue.of(Boolean.parseBoolean(value));
            } else {
                try {
                    return Int32Value.of(Integer.parseInt(value));
                } catch (NumberFormatException e) {
                    try {
                        return BitVectorValue.of(BitVector.parse(value));
                    } catch (Exception ignored) {
                        log.error("Invalid argument value: {}", value);
                        throw new IllegalArgumentException("Invalid argument value: " + value);
                    }
                }
            }
        }
    }

    @CommandLine.Command(
            name = "dump",
            description = "Dump the NetX source code to Verilog"
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
                var builder = Builder.from(Configuration.forCompact().withTop(top));
                var node = builder.loadPath(source).compileAndDump();
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

    @CommandLine.Command(
            name = "visualize",
            description = "Visualize the NetX source code"
    )
    static class VisualizeCommand implements Callable<Integer> {
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
                names = {"-d", "--depth"},
                description = "depth of visualization",
                defaultValue = "1"
        )
        private int depth;

        @CommandLine.Option(
                names = {"-o", "--output"},
                description = "output file"
        )
        private Path output;

        @Override
        public Integer call() {
            try {
                var builder = Builder.from(Configuration.forCompact().withTop(top));
                var compiler = builder.loadPath(source);
                compiler.compileAndVisualize(notepadWriterFor(output), depth);
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
