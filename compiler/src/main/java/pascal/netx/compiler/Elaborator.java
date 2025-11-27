package pascal.netx.compiler;

import pascal.netx.compiler.ast.PackageNode;
import pascal.netx.compiler.ast.element.*;
import pascal.netx.compiler.ast.expr.ExprNode;
import pascal.netx.compiler.circuit.EList;
import pascal.netx.compiler.circuit.Signature;
import pascal.netx.compiler.circuit.expr.ComponentRef;
import pascal.netx.compiler.circuit.scope.ComponentScope;
import pascal.netx.compiler.circuit.scope.PackageScope;
import pascal.netx.compiler.circuit.scope.RepoScope;
import pascal.netx.compiler.circuit.scope.binding.MonoBinding;
import pascal.netx.compiler.circuit.scope.binding.MultiBinding;
import pascal.netx.compiler.circuit.scope.binding.OrderedAttrBindList;
import pascal.netx.compiler.circuit.signal.Array;
import pascal.netx.compiler.circuit.signal.Signal;
import pascal.netx.compiler.circuit.signal.type.ArrayType;
import pascal.netx.compiler.circuit.signal.type.InterfaceType;
import pascal.netx.compiler.circuit.signal.type.Type;
import pascal.netx.compiler.io.Configuration;
import pascal.netx.compiler.utils.CompileCancellationException;
import pascal.netx.compiler.utils.Logger;
import pascal.netx.util.lang.Identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import static pascal.netx.compiler.circuit.Entities.requiresEList;
import static pascal.netx.compiler.circuit.Utils.compilerOf;
import static pascal.netx.compiler.circuit.Utils.requireEqSize;

public final class Elaborator {
    private final PackageNode node;
    private final PackageScope pack;
    private final Configuration config;

    public Elaborator(RepoScope parent, PackageNode node, Configuration config) {
        this.node = node;
        this.config = config;
        this.pack = PackageScope.newPackage(parent);
    }

    public PackageScope elaborate() {
        Identifier name = node.getPackageInfo().getIdentifier();
        Logger.startPackage(name, node.getSourceLoc());
        initializePackageDefs();
        var topNode = findTopNode();
        var topScope = pack.instantiateTop(topNode.getIdentifier());
        bindConfiguredAttributes(topNode, topScope);
        new ComponentElaborator(topScope).build(topNode);
        setSignature(topScope, topNode);
        Logger.finishPackage();
        return pack;
    }

    private void initializePackageDefs() {
        for (var element : node.getElements()) {
            switch (element) {
                case EnumNode _enum -> pack.defs().addEnum(_enum);
                case ImportNode _import -> pack.defs().addImport(_import);
                case ComponentNode _component -> pack.defs().addComponent(_component);
                case InterfaceNode _interface -> pack.defs().addInterface(_interface);
            }
        }
    }

    private ComponentNode findTopNode() {
        return node.getComponents().stream()
                .filter(node -> config.topName().equals(node.getIdentifier()))
                .findAny().orElseGet(() -> {
                    Logger.warn("No component named '{}' found, using the first component as top", config.topName().name());
                    return node.getComponents().getFirst();
                });
    }

    private void bindConfiguredAttributes(ComponentNode topNode, ComponentScope topScope) {
        switch (config.attributes()) {
            case OrderedAttrBindList(var attrs) -> {
                int size = requireEqSize(topNode.getAttributes(), attrs,
                        "The number of configured attributes does not match the number of top component attributes");
                IntStream.range(0, size).forEach(i -> topScope.bind(topNode.getAttributes().get(i), attrs.get(i)));
            }
            case MultiBinding binding -> binding.forEach(topScope::bind);
            case null -> {
                // do nothing
            }
        }
    }

    private void setSignature(ComponentScope topScope, ComponentNode topNode) {
        var atom = ComponentRef.of(topNode, topNode.getIdentifier());
        atom.setArgList(config.attributes());
        topScope.setSignature(Signature.of(atom, topScope.getInputs(), topScope.getOutputs()));
    }

    static final class ComponentElaborator {
        private final ComponentScope scope;
        private final Evaluator evaluator;

        ComponentElaborator(ComponentScope scope) {
            this.scope = scope;
            this.evaluator = scope.getEvaluator();
        }

        public void build(AbstractComponentNode def) {
            Logger.startComponent(def.getIdentifier(), def.getSourceLoc());
            def.getElements().forEach(this::buildElement);
            switch (def) {
                case ComponentNode node -> buildComponentIO(node);
                case InterfaceNode node -> buildInterfaceIO(node);
            }
            Logger.finishComponent();
        }

        private void buildComponentIO(ComponentNode node) {
            var signature = node.getSig();
            extractIOList(signature.getInput()).forEach(scope::addInput);
            extractIOList(signature.getOutput()).forEach(scope::addOutput);
        }

        private void buildInterfaceIO(InterfaceNode node) {
            extractIOList(node.getSig()).forEach(scope::addInput);
            var factory = new Evaluator.InterfaceFactoryCreator(scope).create(node);
            var product = factory.create(scope, Identifier.of(InterfaceNode.OUTPUT_NAME));
            var productIter = product.getElements().iterator();
            for (var input : scope.getInputs()) {
                scope.assigns().add(productIter.next(), input);
            }
            scope.addOutput(product);
        }

        List<Signal> extractIOList(ExprNode node) {
            var entity = evaluator.evaluate(node);
            if (entity instanceof EList list) {
                if (list.stream().anyMatch(Predicate.not(Signal.class::isInstance))) {
                    Logger.error("Expected wires and bundles in IO list but got {}", list);
                    throw new CompileCancellationException();
                }
                return list.stream().map(Signal.class::cast).toList();
            } else if (entity instanceof Signal signal) {
                return List.of(signal);
            }
            Logger.error("Expected wires and bundles in IO list but got {}", entity);
            throw new CompileCancellationException();
        }

        void buildElement(ComponentElement element) {
            Logger.enterLoc(element.getSourceLoc());
            switch (element) {
                case LoopNode loop -> buildLoop(loop);
                case BlockNode block -> buildBlock(block);
                case BranchNode branch -> buildBranch(branch);
                case BindingNode binding -> buildBinding(binding);
                case DataInstantiationNode data -> buildData(data);
                case ComponentInstantiationNode instantiation -> buildSubComponent(instantiation);
            }
            Logger.exitLoc();
        }

        private void buildBlock(BlockNode block) {
            block.getElements().forEach(this::buildElement);
        }

        private void buildLoop(LoopNode loop) {
            var generator = loop.getGenerator();
            for (var entity : requiresEList(evaluator.evaluate(generator.getFrom()))) {
                int count = scope.getBindingCount();
                scope.overrideBindings(MonoBinding.of(generator.getHead(), entity));
                buildElement(loop.getElement());

                // Since there could be let-bindings inside the loop body,
                // when the loop iteration ends, the inside bindings should also be restored.
                while (scope.getBindingCount() > count) {
                    scope.restoreBindings();
                }
            }
        }

        private void buildBranch(BranchNode branch) {
            var condition = evaluator.evaluateAndCastToBoolean(branch.getCondition());
            if (condition.value()) {
                buildElement(branch.getThenBranch());
            } else if (branch.getElseBranch() != null) {
                buildElement(branch.getElseBranch());
            }
        }

        private void buildBinding(BindingNode binding) {
            scope.overrideBindings(evaluator.evaluateBinding(binding));
        }

        private void buildData(DataInstantiationNode node) {
            var baseFactory = evaluator.evaluateToMutableFactory(node.getType());
            switch (node.getKind()) {
                case BUNDLE -> checkFactoryLegality(baseFactory.getType(), true);
                case WIRE -> checkFactoryLegality(baseFactory.getType(), false);
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

        private void checkFactoryLegality(Type type, boolean isBundle) {
            if (type instanceof ArrayType arrayType) {
                checkFactoryLegality(arrayType.baseType(), isBundle);
            } else {
                if (isBundle) {
                    if (!(type instanceof InterfaceType)) {
                        Logger.error("Expected bundle type but got {}", type);
                        throw new CompileCancellationException();
                    }
                } else {
                    if (type instanceof InterfaceType) {
                        Logger.error("Expected wire type but got {}", type);
                        throw new CompileCancellationException();
                    }
                }
            }
        }

        private void buildSubComponent(ComponentInstantiationNode instantiation) {
            boolean inPlace = instantiation.needDeclareInPlace();
            evaluator.evaluate(instantiation.getExpr(), inPlace);
        }
    }
}
