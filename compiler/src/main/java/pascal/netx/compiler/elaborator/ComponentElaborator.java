package pascal.netx.compiler.elaborator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import static pascal.netx.compiler.model.Entities.requiresMutable;
import static pascal.netx.compiler.model.Utils.*;
import static pascal.netx.compiler.model.scope.Attributes.from;

final class ComponentElaborator {
    private static final Logger log = LoggerFactory.getLogger(ComponentElaborator.class);
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
            case ComponentNode node -> buildComponent(node);
            case InterfaceNode node -> buildInterface(node);
        }
    }

    public void buildComponent(ComponentNode node) {
        buildElements(node.getElements());
        buildComponentIO(node);
    }

    public void buildInterface(InterfaceNode node) {
        buildElements(node.getElements());
        buildInterfaceIO(node);
    }

    void buildComponentIO(ComponentNode node) {
        var sourceLoc = node.getSourceLoc();
        var type = node.getType();
        extractIOList(sourceLoc, type.getInput()).forEach(scope::addInput);
        extractIOList(sourceLoc, type.getOutput()).forEach(scope::addOutput);
    }

    void buildInterfaceIO(InterfaceNode node) {
        var loc = node.getSourceLoc();
        var def = scope.getPackage().defs().getInterface(node.getIdentifier());
        var factory = new InterfaceFactoryMaker(scope).make(def);
        extractIOList(loc, node.getType()).forEach(scope::addInput);
        var product = factory.create(scope, Identifier.of(InterfaceNode.OUTPUT_NAME).setSourceLoc(loc));
        var productIter = product.getElements().iterator();
        for (var input : scope.getInputs()) {
            scope.getManager().assign(loc, productIter.next(), input);
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
                    log.error("{}: Expected list of instances but got {}", sourceLoc, list);
                    throw new CompileCancellationException();
                }
            }
            return result;
        } else if (entity instanceof Variable variable) {
            return List.of(variable);
        }
        log.error("{}: Expected list of instances but got {}", sourceLoc, entity);
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
                    log.error("{}: Expected bundle type got {}", sourceLoc, type);
                    throw new CompileCancellationException();
                }
            } else {
                if (type instanceof InterfaceType) {
                    log.error("{}: Expected wire type but got {}", sourceLoc, type);
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

        /**
         * Used to infer the number of inputs for builtin components in certain pattern (needHintForContinuationOf)
         * argsHint is set to -1 to indicate that the hint is not needed.
         */
        private int argsHint = -1;

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
                case PortConnectionExpr connection -> instantiatePortConnectionExpr(connection);
            };
        }

        private ComponentPort instantiateDecoratedExpr(DecoratedExpr expr) {
            var port = doInstantiate(expr.operand());
            return switch (expr.decorator()) {
                case SOURCE -> ComponentPort.of(port.target, List.of());
                case SINK -> ComponentPort.of(List.of(), port.source);
                case BIDI -> port;
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

        private ComponentPort instantiateSequentialExpr(SequentialExpr sequentialExpr) {
            // right-hand-side goes first
            ComponentPort lhs, rhs;
            rhs = doInstantiate(sequentialExpr.rhs());
            if (needHintForContinuationOf(sequentialExpr.lhs())) {
                argsHint = rhs.target().size();
            }
            lhs = doInstantiate(sequentialExpr.lhs());

            int size = requiresMatchedSize(sourceLoc, lhs.source(), rhs.target());
            for (int i = 0; i < size; ++i) {
                scope.getManager().assign(sourceLoc, lhs.source().get(i), rhs.target().get(i));
            }
            return ComponentPort.of(lhs.target(), rhs.source());
        }

        private ComponentPort instantiatePortConnectionExpr(PortConnectionExpr connection) {
            var loc = connection.loc();
            var lhsScope = switch (connection.instance()) {
                case ComponentRef componentRef -> instantiateComponentRef(componentRef);
                case BuiltinRef builtinRef -> instantiateBuiltinRef(builtinRef);
                case ImportRef importRef -> instantiateImportRef(importRef);
            };

            var lhs = ComponentPort.of(lhsScope);
            Map<Mutable, ComponentPort> assigns = new HashMap<>();
            for (var entry : connection.ports()) {
                var port = doInstantiate(entry.rhs());
                if (!(port.target().size() == 1)) {
                    log.error("{}: Expect exactly one output of {} but got {}", loc, entry.rhs(), port.target());
                    throw new CompileCancellationException();
                }
                var receiver = requiresMutable(loc, lhsScope.getEvaluator().evaluate(entry.lhs()));
                assigns.put(receiver, port);
            }

            requiresMatchedSize(connection.loc(), assigns.size(), lhs.source());
            for (var variable : lhs.source()) {
                if (!(variable instanceof Mutable mutable)) {
                    log.error("{}: Expected mutable variable but got {}", connection.loc(), variable);
                    throw new CompileCancellationException();
                }
                if (!assigns.containsKey(mutable)) {
                    log.error("{}: Missing assignment for {}", connection.loc(), variable);
                    throw new CompileCancellationException();
                }
                scope.getManager().assign(connection.loc(), mutable, assigns.get(mutable).target().getFirst());
            }
            return ComponentPort.of(lhs.target, List.of());
        }

        private ComponentPort instantiateParallelExpr(ParallelExpr parallelExpr) {
            List<Variable> input = new LinkedList<>(), output = new LinkedList<>();
            for (var sub : parallelExpr.exprs()) {
                var port = doInstantiate(sub);
                input.addAll(port.target());
                output.addAll(port.source());
            }
            return ComponentPort.of(input, output);
        }

        private ComponentPort instantiateInstanceRef(VariableRef ref) {
            var inst = List.of(ref.getInstance());
            if (ref.getArgList() != null) {
                log.error("{}: Unexpected arguments in instance reference {}", sourceLoc, ref.getInstance());
                throw new CompileCancellationException();
            }
            if (ref.getInstance() instanceof Mutable) {
                return ComponentPort.of(inst, inst);
            } else {
                return ComponentPort.of(inst, List.of());
            }
        }

        private ComponentScope instantiateImportRef(ImportRef ref) {
            SourceLoc sourceLoc = ref.getSourceLoc();

            var compiler = repoOf(scope).getManager().dispatchCompiler(sourceLoc, ref);
            var pack = compiler.getPackManager().resolvePackage(sourceLoc, ref.packageRef());
            var component = compiler.elaborateComponent(pack, ref.componentRef(), ref.getArgList());

            component.setParent(scope);
            component.setPackage(scope.getPackage());
            return component;
        }

        private ComponentScope instantiateBuiltinRef(BuiltinRef ref) {
            ComponentScope component;
            if (argsHint != -1) {
                component = factory.createBuiltInComponent(scope, ref, argsHint);
                argsHint = -1;
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
            if (!config.fullyExpand()) {
                setSignatureFor(ref, component);
            }
            return component;
        }

        private void setSignatureFor(ComponentRef ref, ComponentScope scope) {
            var signature = Signature.of(ref, scope.getInputs(), scope.getOutputs());
            scope.setSignature(signature);
        }


        private record ComponentPort(List<Variable> target, List<Variable> source) {
            public static ComponentPort of(List<Variable> target, List<Variable> source) {
                return new ComponentPort(target, source);
            }

            public static ComponentPort of(ComponentScope scope) {
                return new ComponentPort(scope.getOutputs(), scope.getInputs());
            }
        }
    }
}
