package pascal.netx.compiler.backend;

import pascal.netx.compiler.io.Logger;
import pascal.netx.compiler.model.circuit.Signature;
import pascal.netx.compiler.model.circuit.Utils;
import pascal.netx.compiler.model.circuit.expr.*;
import pascal.netx.compiler.model.circuit.scope.ComponentScope;
import pascal.netx.compiler.model.circuit.signal.Array;
import pascal.netx.compiler.model.circuit.signal.Composition;
import pascal.netx.compiler.model.circuit.signal.Data;
import pascal.netx.compiler.model.circuit.signal.Signal;
import pascal.netx.compiler.utils.CompileCancellationException;
import pascal.netx.util.lang.SourceLoc;

import java.util.*;
import java.util.stream.Stream;

import static pascal.netx.compiler.model.circuit.Entities.requireData;
import static pascal.netx.compiler.model.circuit.Utils.compilerOf;
import static pascal.netx.compiler.model.circuit.Utils.repoOf;
import static pascal.netx.compiler.model.circuit.scope.Attributes.from;

final class Connector {
    private final ComponentScope scope;
    private final BuiltInComponentFactory factory;
    private Integer argNumHint = null;

    Connector(ComponentScope scope) {
        this.scope = scope;
        this.factory = new BuiltInComponentFactory(compilerOf(scope).getIndexer());
    }

    private static boolean needHintForContinuationOf(Expr expr) {
        return switch (expr) {
            case BuiltinRef _ -> true;
            case SequentialExpr e -> needHintForContinuationOf(e.rhs());
            default -> false;
        };
    }

    Port connect(Expr expr) {
        Logger.enterLoc(expr.getSourceLoc());
        var result = switch (expr) {
            case PrimaryExpr primary -> connectPrimaryExpr(primary);
            case ParallelExpr parallel -> connectParallelExpr(parallel);
            case DecoratedExpr decorated -> connectDecoratedExpr(decorated);
            case SequentialExpr sequential -> connectSequentialExpr(sequential);
            case NamedPortExpr _ -> {
                Logger.error("Unexpected Port Connect Expression {}", expr);
                throw new CompileCancellationException();
            }
        };
        Logger.exitLoc();
        return result;
    }

    private Port connectDecoratedExpr(DecoratedExpr expr) {
        var port = connect(expr.operand());
        return switch (expr.decorator()) {
            case BLOCK_IN -> Port.ofBlast(List.of(), port.output);
            case BLOCK_OUT -> Port.ofBlast(port.input, List.of());
            case NON_BLOCK -> port;
        };
    }

    private Port connectPrimaryExpr(PrimaryExpr expr) {
        return switch (expr) {
            case ComponentRef ref -> Port.ofBlast(instantiateComponentRef(ref));
            case BuiltinRef ref -> Port.ofBlast(instantiateBuiltinRef(ref));
            case ImportRef ref -> Port.ofBlast(instantiateImportRef(ref));
            case SignalRef ref -> connectInstanceRef(ref);
        };
    }

    private Port connectSequentialExpr(SequentialExpr expr) {
        if (expr.rhs() instanceof NamedPortExpr port) {
            return connectByName(instantiateScope(expr.lhs()), port, true);
        }
        if (expr.lhs() instanceof NamedPortExpr lhs) {
            if (expr.rhs() instanceof SequentialExpr(Expr mid, NamedPortExpr rhs)) {
                var midInst = instantiateScope(mid);
                connectByName(midInst, rhs, true);
                connectByName(midInst, lhs, false);
                return Port.ofEmpty();
            } else {
                return connectByName(instantiateScope(expr.rhs()), lhs, false);
            }
        }

        // right-hand-side goes first
        Port lhs, rhs;
        rhs = connect(expr.rhs());
        if (needHintForContinuationOf(expr.lhs())) {
            argNumHint = rhs.output().size();
        }
        lhs = connect(expr.lhs());

        int size = Utils.requireEqSize(lhs.input(), rhs.output(),
                "Sequential connection requires matched port size");
        for (int i = 0; i < size; ++i) {
            scope.assigns().add(lhs.input().get(i), rhs.output().get(i));
        }

        return Port.ofBlast(rhs.input(), lhs.output());
    }

    private Port connectByName(ComponentScope inst, NamedPortExpr connection, boolean isInput) {
        return new NamedPortConnector(inst, connection, isInput).connect();
    }

    private Port connectParallelExpr(ParallelExpr parallelExpr) {
        List<Signal> output = new LinkedList<>(), input = new LinkedList<>();
        for (var sub : parallelExpr.exprs()) {
            var port = connect(sub);
            output.addAll(port.output());
            input.addAll(port.input());
        }
        return Port.ofBlast(input, output);
    }

    private Port connectInstanceRef(SignalRef ref) {
        var inst = List.of(ref.getInstance());
        if (ref.getArgList() != null) {
            Logger.error("Unexpected attributes in instance reference {}", ref.getInstance());
            throw new CompileCancellationException();
        }
        if (ref.getInstance() instanceof Data) {
            return Port.ofBlast(inst, inst);
        } else {
            return Port.ofBlast(List.of(), inst);
        }
    }

    private record Port(List<Signal> input, List<Signal> output) {
        public static Port ofBlast(List<Signal> input, List<Signal> output) {
            return new Port(blast(input), blast(output));
        }

        public static Port ofBlast(ComponentScope scope) {
            return ofBlast(scope.getInputs(), scope.getOutputs());
        }

        public static Port ofEmpty() {
            return new Port(List.of(), List.of());
        }

        private static List<Signal> blast(List<Signal> input) {
            return input.stream().flatMap(Port::blast).toList();
        }

        private static Stream<Signal> blast(Signal sig) {
            if (sig instanceof Array arr) {
                return arr.getElements().stream().flatMap(Port::blast);
            } else {
                return Stream.of(sig);
            }
        }
    }

    private ComponentScope instantiateScope(Expr expr) {
        return switch (expr) {
            case ImportRef ref -> instantiateImportRef(ref);
            case BuiltinRef ref -> instantiateBuiltinRef(ref);
            case ComponentRef ref -> instantiateComponentRef(ref);
            default -> {
                Logger.error("Unexpected expression type {}", expr);
                throw new CompileCancellationException();
            }
        };
    }

    private ComponentScope instantiateImportRef(ImportRef ref) {
        SourceLoc sourceLoc = ref.getSourceLoc();

        var compiler = repoOf(scope).getManager().dispatchCompiler(sourceLoc, ref);
        var pack = compiler.getPackManager().resolvePackage(sourceLoc, ref.packageRef());
        var component = compiler.elaborateComponent(pack, ref.componentRef(), ref.getArgList());

        if (ref.getArgList() != null) {
            var attributes = from(pack.resolveComponent(ref.componentRef()), ref.getArgList());
            component.setAttributes(attributes);
        }
        component.setParent(scope);
        component.setPackage(scope.getPackage());
        return component;
    }

    private ComponentScope instantiateBuiltinRef(BuiltinRef ref) {
        ComponentScope component;
        if (argNumHint != null) {
            component = factory.createBuiltInComponent(scope, ref, argNumHint);
            argNumHint = null;
        } else {
            component = factory.createBuiltInComponent(scope, ref);
        }
        return component;
    }

    private ComponentScope instantiateComponentRef(ComponentRef ref) {
        var node = ref.getDefinition();
        var component = ref.isInterface()
                ? ComponentScope.newInterface(scope, ref.getIdentifier(), true)
                : ComponentScope.newComponent(scope, ref.getIdentifier());

        var attributes = from(node, ref.getArgList());
        for (var entry : attributes.entrySet()) {
            component.bind(entry.getKey(), entry.getValue());
        }
        component.setAttributes(attributes);
        new Elaborator.ComponentElaborator(component).build(node);
        component.setSignature(Signature.of(ref, component.getInputs(), component.getOutputs()));
        return component;
    }

    private final class NamedPortConnector {
        private final ComponentScope inst;
        private final NamedPortExpr connection;
        private final boolean isInput;

        private final Port defaultPort;
        private final Map<Signal, Signal> assigns = new HashMap<>();
        private final Map<Composition, List<Signal>> compAssigns = new HashMap<>();

        private NamedPortConnector(ComponentScope inst, NamedPortExpr connection, boolean isInput) {
            this.inst = inst;
            this.connection = connection;
            this.isInput = isInput;
            buildAssignMap();
            this.defaultPort = resolveDefaultPort();
        }

        public Port connect() {
            for (var sig : isInput ? inst.getInputs() : inst.getOutputs()) {
                if (sig instanceof Composition composition) {
                    assignForComposition(composition);
                } else {
                    assignForSingleton(sig);
                }
            }
            if (isInput) {
                return Port.ofBlast(List.of(), inst.getOutputs());
            } else {
                return Port.ofBlast(inst.getInputs(), List.of());
            }
        }

        private void buildAssignMap() {
            for (var entry : connection.getPorts()) {
                var conn = Connector.this.connect(entry.expr());
                var namedPort = requireData(inst.getEvaluator().evaluate(entry.port()));

                if (namedPort instanceof Composition composition) {
                    compAssigns.put(composition, Port.blast(targetOf(conn)));
                } else {
                    assigns.put(requireData(namedPort), exactTargetOf(conn));
                }
            }
        }

        private void assign(Signal port, Signal target) {
            if (isInput) {
                scope.assigns().add(port, target);
            } else {
                scope.assigns().add(target, port);
            }
        }

        private Port resolveDefaultPort() {
            Port defaultPort = null;
            if (connection.hasDefault()) {
                defaultPort = Connector.this.connect(connection.getDefaultExpr());
            }
            return defaultPort;
        }

        private void assignForComposition(Composition composition) {
            var targetList = getListOrDefault(composition);
            var portList = Port.blast(composition).toList();
            int size = Utils.requireEqSize(portList, targetList,
                    "Bundle or array port with size mismatch for " + composition);
            for (int i = 0; i < size; ++i) {
                assign(portList.get(i), targetList.get(i));
            }
        }

        private void assignForSingleton(Signal signal) {
            assign(signal, getOrDefault(signal));
        }

        private List<Signal> getListOrDefault(Composition composition) {
            return Optional.ofNullable(compAssigns.get(composition)).orElseGet(() -> {
                if (defaultPort == null) {
                    Logger.error("No assignment found for port {}", composition);
                    throw new CompileCancellationException();
                }
                return targetOf(defaultPort);
            });
        }

        private Signal getOrDefault(Signal signal) {
            return Optional.ofNullable(assigns.get(signal)).orElseGet(() -> {
                if (defaultPort == null) {
                    Logger.error("No assignment found for port {}", signal);
                    throw new CompileCancellationException();
                }
                return exactTargetOf(defaultPort);
            });
        }

        private List<Signal> targetOf(Port port) {
            return isInput ? port.output : port.input;
        }

        private Signal exactTargetOf(Port port) {
            var targets = targetOf(port);
            if (targets.size() != 1) {
                Logger.error("Expected exactly one target signal but got {}", targets);
                throw new CompileCancellationException();
            }
            return targets.getFirst();
        }
    }
}
