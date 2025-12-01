package pascal.netx.compiler.backend;

import pascal.netx.compiler.io.Logger;
import pascal.netx.compiler.model.circuit.Signature;
import pascal.netx.compiler.model.circuit.Utils;
import pascal.netx.compiler.model.circuit.expr.*;
import pascal.netx.compiler.model.circuit.scope.ComponentScope;
import pascal.netx.compiler.model.circuit.signal.Data;
import pascal.netx.compiler.model.circuit.signal.Signal;
import pascal.netx.compiler.utils.CompileCancellationException;
import pascal.netx.util.lang.SourceLoc;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static pascal.netx.compiler.model.circuit.Entities.requireMutable;
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
            case BLOCK_IN -> new Port(List.of(), port.output);
            case BLOCK_OUT -> new Port(port.input, List.of());
            case NON_BLOCK -> port;
        };
    }

    private Port connectPrimaryExpr(PrimaryExpr expr) {
        return switch (expr) {
            case ComponentRef ref -> Port.of(instantiateComponentRef(ref));
            case BuiltinRef ref -> Port.of(instantiateBuiltinRef(ref));
            case ImportRef ref -> Port.of(instantiateImportRef(ref));
            case SignalRef ref -> connectInstanceRef(ref);
        };
    }

    private Port connectSequentialExpr(SequentialExpr expr) {
        if (expr.rhs() instanceof NamedPortExpr port) {
            return doPortConnection(instantiateScope(expr.lhs()), port, true);
        }
        if (expr.lhs() instanceof NamedPortExpr lhs) {
            if (expr.rhs() instanceof SequentialExpr(Expr mid, NamedPortExpr rhs)) {
                var midInst = instantiateScope(mid);
                doPortConnection(midInst, rhs, true);
                doPortConnection(midInst, lhs, false);
                return Port.ofEmpty();
            } else {
                return doPortConnection(instantiateScope(expr.rhs()), lhs, false);
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
        return new Port(rhs.input(), lhs.output());
    }

    private Port doPortConnection(ComponentScope inst, NamedPortExpr connection, boolean isInput) {
        var instPort = Port.of(inst);

        Function<Port, List<Signal>> getter =
                (Port port) -> (isInput ? port.output : port.input);

        Map<Data, Port> assigns = new HashMap<>();
        for (var entry : connection.getPorts()) {
            var conn = connect(entry.expr());
            if (!(getter.apply(conn).size() == 1)) {
                Logger.error("Expect exactly one output of {} but got", entry.expr(), conn.output());
                throw new CompileCancellationException();
            }
            var port = inst.getEvaluator().evaluate(entry.port());
            assigns.put(requireMutable(port), conn);
        }

        Port defaultPort = null;
        var port = isInput ? instPort.input() : instPort.output();
        if (!connection.hasDefault()) {
            Utils.requireEqSize(assigns.size(), port,
                    "Port connection requires all ports to be connected or provide a default connection");
        } else {
            defaultPort = connect(connection.getDefaultExpr());
        }

        for (var variable : port) {
            if (!(variable instanceof Data data)) {
                Logger.error("Expected mutable variable but got {}", variable);
                throw new CompileCancellationException();
            }

            var expr = assigns.getOrDefault(data, defaultPort);
            if (isInput) {
                scope.assigns().add(data, getter.apply(expr).getFirst());
            } else {
                scope.assigns().add(getter.apply(expr).getFirst(), data);
            }
        }

        if (isInput) {
            return new Port(List.of(), instPort.output);
        } else {
            return new Port(instPort.input, List.of());
        }
    }

    private Port connectParallelExpr(ParallelExpr parallelExpr) {
        List<Signal> output = new LinkedList<>(), input = new LinkedList<>();
        for (var sub : parallelExpr.exprs()) {
            var port = connect(sub);
            output.addAll(port.output());
            input.addAll(port.input());
        }
        return new Port(input, output);
    }

    private Port connectInstanceRef(SignalRef ref) {
        var inst = List.of(ref.getInstance());
        if (ref.getArgList() != null) {
            Logger.error("Unexpected attributes in instance reference {}", ref.getInstance());
            throw new CompileCancellationException();
        }
        if (ref.getInstance() instanceof Data) {
            return new Port(inst, inst);
        } else {
            return new Port(List.of(), inst);
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

    private record Port(List<Signal> input, List<Signal> output) {
        public static Port of(ComponentScope scope) {
            return new Port(scope.getInputs(), scope.getOutputs());
        }

        public static Port ofEmpty() {
            return new Port(List.of(), List.of());
        }
    }
}
