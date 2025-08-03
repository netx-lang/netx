package pascal.netx.compiler.elaborator;

import pascal.netx.compiler.ast.element.*;
import pascal.netx.compiler.ast.expr.ExprNode;
import pascal.netx.compiler.exception.CompileCancellationException;
import pascal.netx.compiler.io.Configuration;
import pascal.netx.compiler.model.EntityList;
import pascal.netx.compiler.model.Signature;
import pascal.netx.compiler.model.expr.*;
import pascal.netx.compiler.model.scope.ComponentScope;
import pascal.netx.compiler.model.scope.binding.MonoBinding;
import pascal.netx.compiler.model.variable.Array;
import pascal.netx.compiler.model.variable.Mutable;
import pascal.netx.compiler.model.variable.Variable;
import pascal.netx.compiler.model.variable.type.ArrayType;
import pascal.netx.compiler.model.variable.type.InterfaceType;
import pascal.netx.compiler.model.variable.type.Type;
import pascal.netx.util.lang.Identifier;
import pascal.netx.util.lang.SourceLoc;

import java.util.*;
import java.util.function.Function;

import static pascal.netx.compiler.model.Entities.requiresMutable;
import static pascal.netx.compiler.model.Utils.*;
import static pascal.netx.compiler.model.scope.Attributes.from;

final class ComponentElaborator {
    private final ComponentScope scope;
    private final ComponentEvaluator evaluator;

    private final Configuration config;
    private final BuiltInComponentFactory factory;

    ComponentElaborator(Configuration config, ComponentScope scope) {
        this.config = config;
        this.scope = scope;
        this.factory = BuiltInComponentFactory.create(compilerOf(scope));
        this.evaluator = scope.getEvaluator();
    }

    public void build(AbstractComponentNode def) {
        switch (def) {
            case ComponentNode node -> {
                Identifier name = node.getIdentifier();
                Logger.startComponent(name, node.getSourceLoc());
                try {
                    buildComponent(node);
                } finally {
                    Logger.finishComponent();
                }
            }
            case InterfaceNode node -> {
                Identifier name = node.getIdentifier();
                Logger.startComponent(name, node.getSourceLoc());
                try {
                    buildInterface(node);
                } finally {
                    Logger.finishComponent();
                }
            }
        }
    }

    private void buildComponent(ComponentNode node) {
        buildElements(node.getElements());
        buildComponentIO(node);
    }

    private void buildInterface(InterfaceNode node) {
        buildElements(node.getElements());
        buildInterfaceIO(node);
    }

    private void buildComponentIO(ComponentNode node) {
        var sourceLoc = node.getSourceLoc();
        var type = node.getType();
        extractIOList(sourceLoc, type.getInput()).forEach(scope::addInput);
        extractIOList(sourceLoc, type.getOutput()).forEach(scope::addOutput);
    }

    private void buildInterfaceIO(InterfaceNode node) {
        var loc = node.getSourceLoc();
        var factory = new InterfaceFactoryMaker(scope).make(node);
        extractIOList(loc, node.getType()).forEach(scope::addInput);
        var product = factory.create(scope, Identifier.of(InterfaceNode.OUTPUT_NAME).setSourceLoc(loc));
        var productIter = product.getElements().iterator();
        for (var input : scope.getInputs()) {
            scope.assigns().add(loc, productIter.next(), input);
        }
        scope.addOutput(product);
    }

    List<Variable> extractIOList(SourceLoc sourceLoc, ExprNode node) {
        var entity = evaluator.evaluate(node);
        if (entity instanceof EntityList(var list)) {
            List<Variable> result = new ArrayList<>();
            for (var member : list) {
                if (member instanceof Variable variable) {
                    result.add(variable);
                } else {
                    Logger.error(sourceLoc, "Expected list of instances but got {}", list);
                    throw new CompileCancellationException();
                }
            }
            return result;
        } else if (entity instanceof Variable variable) {
            return List.of(variable);
        }
        Logger.error(sourceLoc, "Expected list of instances but got {}", entity);
        throw new CompileCancellationException();
    }

    void buildElements(List<ComponentElement> list) {
        for (var element : list) {
            buildElement(element);
        }
    }

    private void buildElement(ComponentElement element) {
        switch (element) {
            case LoopNode loop -> buildLoop(loop);
            case BlockNode block -> buildBlock(block);
            case BranchNode branch -> buildBranch(branch);
            case BindingNode binding -> buildBinding(binding);
            case DataInstantiationNode data -> buildData(data);
            case ComponentInstantiationNode instantiation -> buildSubComponent(instantiation);
        }
    }

    private void buildBlock(BlockNode block) {
        buildElements(block.getElements());
    }

    private void buildLoop(LoopNode loop) {
        var generator = loop.getGenerator();
        var id = generator.getHead();
        for (var entity : evaluator.castToList(generator.getFrom()).entities()) {
            int count = scope.getBindingCount();
            scope.overrideBindings(MonoBinding.of(id, entity));
            buildElement(loop.getElement());
            while (scope.getBindingCount() > count) {
                scope.restoreBindings();
            }
        }
    }

    private void buildBranch(BranchNode branch) {
        var condition = evaluator.evaluateToBoolean(branch.getCondition());
        if (condition.value()) {
            buildElement(branch.getThenBranch());
        } else {
            buildElement(branch.getElseBranch());
        }
    }

    private void buildBinding(BindingNode binding) {
        scope.overrideBindings(evaluator.evaluateBinding(binding));
    }

    private void buildData(DataInstantiationNode node) {
        var baseFactory = evaluator.evaluateToMutableFactory(node.getType());
        switch (node.getKind()) {
            case BUNDLE -> checkFactoryLegality(node.getSourceLoc(), baseFactory.getType(), true);
            case WIRE -> checkFactoryLegality(node.getSourceLoc(), baseFactory.getType(), false);
        }
        for (var rest : node.getDataRests()) {
            var factory = baseFactory;
            if (!rest.getDimensions().isEmpty()) {
                var dimensions = new ArrayList<>(rest.getDimensions());
                Collections.reverse(dimensions);
                for (ExprNode dimension : dimensions) {
                    var size = evaluator.evaluateToInt(dimension);
                    factory = new Array.Factory(compilerOf(scope), size, factory);
                }
            }
            var id = rest.getIdentifier();
            scope.bind(id, factory.create(scope, id));
        }
    }

    private void checkFactoryLegality(SourceLoc sourceLoc, Type type, boolean isBundle) {
        if (type instanceof ArrayType arrayType) {
            checkFactoryLegality(sourceLoc, arrayType.baseType(), isBundle);
        } else {
            if (isBundle) {
                if (!(type instanceof InterfaceType)) {
                    Logger.error(sourceLoc, "Expected bundle type but got {}", type);
                    throw new CompileCancellationException();
                }
            } else {
                if (type instanceof InterfaceType) {
                    Logger.error(sourceLoc, "Expected wire type but got {}", type);
                    throw new CompileCancellationException();
                }
            }
        }
    }

    private void buildSubComponent(ComponentInstantiationNode instantiation) {
        var sourceLoc = instantiation.getSourceLoc();
        boolean inPlace = instantiation.needDeclareInPlace();
        var compType = evaluator.evaluateToComponentExpr(instantiation.getExpr(), inPlace);
        new Instantiator(sourceLoc).instantiate(compType);
    }

    private class Instantiator {
        private final SourceLoc sourceLoc;

        private Integer argNumHint = null;

        private Instantiator(SourceLoc sourceLoc) {
            this.sourceLoc = sourceLoc;
        }

        private static boolean needHintForContinuationOf(Expr expr) {
            return switch (expr) {
                case BuiltinRef _ -> true;
                case SequentialExpr e -> needHintForContinuationOf(e.rhs());
                default -> false;
            };
        }

        public void instantiate(Expr type) {
            doInstantiate(type);
        }

        ComponentPort doInstantiate(Expr expr) {
            return switch (expr) {
                case PrimaryExpr primary -> instantiatePrimaryExpr(primary);
                case ParallelExpr parallel -> instantiateParallelExpr(parallel);
                case DecoratedExpr decorated -> instantiateDecoratedExpr(decorated);
                case SequentialExpr sequential -> instantiateSequentialExpr(sequential);
                case PortExpr connection -> {
                    Logger.error(connection.loc(), "Unexpected Port Connect Expression {}", connection);
                    throw new CompileCancellationException();
                }
            };
        }

        private ComponentPort instantiateDecoratedExpr(DecoratedExpr expr) {
            var port = doInstantiate(expr.operand());
            return switch (expr.decorator()) {
                case BLOCK_IN -> new ComponentPort(List.of(), port.output);
                case BLOCK_OUT -> new ComponentPort(port.input, List.of());
                case NON_BLOCK -> port;
            };
        }

        private ComponentPort instantiatePrimaryExpr(PrimaryExpr expr) {
            return switch (expr) {
                case ComponentRef ref -> ComponentPort.of(instantiateComponentRef(ref));
                case BuiltinRef ref -> ComponentPort.of(instantiateBuiltinRef(ref));
                case ImportRef ref -> ComponentPort.of(instantiateImportRef(ref));
                case VariableRef ref -> instantiateInstanceRef(ref);
            };
        }

        private ComponentPort instantiateSequentialExpr(SequentialExpr expr) {
            if (expr.rhs() instanceof PortExpr port) {
                return doPortConnection(instantiateScope(expr.lhs()), port, true);
            }
            if (expr.lhs() instanceof PortExpr lhs) {
                if (expr.rhs() instanceof SequentialExpr(Expr mid, PortExpr rhs)) {
                    var midInst = instantiateScope(mid);
                    doPortConnection(midInst, rhs, true);
                    doPortConnection(midInst, lhs, false);
                    return ComponentPort.ofEmpty();
                } else {
                    return doPortConnection(instantiateScope(expr.rhs()), lhs, false);
                }
            }

            // right-hand-side goes first
            ComponentPort lhs, rhs;
            rhs = doInstantiate(expr.rhs());
            if (needHintForContinuationOf(expr.lhs())) {
                argNumHint = rhs.output().size();
            }
            lhs = doInstantiate(expr.lhs());

            int size = requiresMatchedSize(sourceLoc, lhs.input(), rhs.output());
            for (int i = 0; i < size; ++i) {
                scope.assigns().add(sourceLoc, lhs.input().get(i), rhs.output().get(i));
            }
            return new ComponentPort(rhs.input(), lhs.output());
        }

        private ComponentPort doPortConnection(ComponentScope inst, PortExpr connection, boolean isInput) {
            var loc = connection.loc();
            var instPort = ComponentPort.of(inst);
            Map<Mutable, ComponentPort> assigns = new HashMap<>();
            Function<ComponentPort, List<Variable>> getter =
                    (ComponentPort port) -> (isInput ? port.output : port.input);

            for (var entry : connection.ports()) {
                var conn = doInstantiate(entry.expr());
                if (!(getter.apply(conn).size() == 1)) {
                    Logger.error(loc, "Expect exactly one output of {} but got", entry.expr(), conn.output());
                    throw new CompileCancellationException();
                }
                var port = inst.getEvaluator().evaluate(entry.port());
                assigns.put(requiresMutable(loc, port), conn);
            }
            var port = isInput ? instPort.input() : instPort.output();
            requiresMatchedSize(connection.loc(), assigns.size(), port);
            for (var variable : port) {
                if (!(variable instanceof Mutable mutable)) {
                    Logger.error(connection.loc(), "Expected mutable variable but got {}", variable);
                    throw new CompileCancellationException();
                }
                if (!assigns.containsKey(mutable)) {
                    Logger.error(connection.loc(), "Missing assignment for {}", variable);
                    throw new CompileCancellationException();
                }

                var expr = assigns.get(mutable);
                if (isInput) {
                    scope.assigns().add(connection.loc(),
                            mutable, getter.apply(expr).getFirst());
                } else {
                    scope.assigns().add(connection.loc(),
                            getter.apply(expr).getFirst(), mutable);
                }
            }
            if (isInput) {
                return new ComponentPort(List.of(), instPort.output);
            } else {
                return new ComponentPort(instPort.input, List.of());
            }
        }

        private ComponentPort instantiateParallelExpr(ParallelExpr parallelExpr) {
            List<Variable> output = new LinkedList<>(), input = new LinkedList<>();
            for (var sub : parallelExpr.exprs()) {
                var port = doInstantiate(sub);
                output.addAll(port.output());
                input.addAll(port.input());
            }
            return new ComponentPort(input, output);
        }

        private ComponentPort instantiateInstanceRef(VariableRef ref) {
            var inst = List.of(ref.getInstance());
            if (ref.getArgList() != null) {
                Logger.error(sourceLoc, "Unexpected arguments in instance reference {}", ref.getInstance());
                throw new CompileCancellationException();
            }
            if (ref.getInstance() instanceof Mutable) {
                return new ComponentPort(inst, inst);
            } else {
                return new ComponentPort(List.of(), inst);
            }
        }

        private ComponentScope instantiateScope(Expr expr) {
            return switch (expr) {
                case ImportRef ref -> instantiateImportRef(ref);
                case BuiltinRef ref -> instantiateBuiltinRef(ref);
                case ComponentRef ref -> instantiateComponentRef(ref);
                default -> {
                    Logger.error(sourceLoc, "Unexpected expression type {}", expr);
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
                var attributes = from(sourceLoc, pack.resolveComponent(ref.componentRef()), ref.getArgList());
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

            var attributes = from(sourceLoc, node, ref.getArgList());
            for (var entry : attributes.entrySet()) {
                component.bind(entry.getKey(), entry.getValue());
            }
            component.setAttributes(attributes);
            new ComponentElaborator(config, component).build(node);
            setSignatureFor(ref, component);
            return component;
        }

        private void setSignatureFor(ComponentRef ref, ComponentScope scope) {
            var signature = Signature.of(ref, scope.getInputs(), scope.getOutputs());
            scope.setSignature(signature);
        }

        private record ComponentPort(List<Variable> input, List<Variable> output) {
            public static ComponentPort of(ComponentScope scope) {
                return new ComponentPort(scope.getInputs(), scope.getOutputs());
            }

            public static ComponentPort ofEmpty() {
                return new ComponentPort(List.of(), List.of());
            }
        }
    }
}
