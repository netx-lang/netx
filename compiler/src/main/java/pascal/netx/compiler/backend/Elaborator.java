package pascal.netx.compiler.backend;

import pascal.netx.compiler.backend.analysis.ast.FreeVariableDetector;
import pascal.netx.compiler.io.Configuration;
import pascal.netx.compiler.io.Logger;
import pascal.netx.compiler.model.ast.PackageNode;
import pascal.netx.compiler.model.ast.element.*;
import pascal.netx.compiler.model.ast.expr.CallExpr;
import pascal.netx.compiler.model.ast.expr.ExprNode;
import pascal.netx.compiler.model.ast.expr.IdentifierExpr;
import pascal.netx.compiler.model.circuit.EList;
import pascal.netx.compiler.model.circuit.Macro;
import pascal.netx.compiler.model.circuit.Signature;
import pascal.netx.compiler.model.circuit.expr.ComponentRef;
import pascal.netx.compiler.model.circuit.scope.ComponentScope;
import pascal.netx.compiler.model.circuit.scope.PackageScope;
import pascal.netx.compiler.model.circuit.scope.RepoScope;
import pascal.netx.compiler.model.circuit.scope.binding.Binding;
import pascal.netx.compiler.model.circuit.scope.binding.IdEntityMap;
import pascal.netx.compiler.model.circuit.scope.binding.OrderedAttrBindList;
import pascal.netx.compiler.model.circuit.signal.Array;
import pascal.netx.compiler.model.circuit.signal.Signal;
import pascal.netx.compiler.utils.CompileCancellationException;
import pascal.netx.util.lang.Identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import static pascal.netx.compiler.model.circuit.Entities.requireEList;
import static pascal.netx.compiler.model.circuit.Utils.compilerOf;
import static pascal.netx.compiler.model.circuit.Utils.requireEqSize;

public record Elaborator(PackageScope scope, PackageNode node, Configuration config) {
    public Elaborator(RepoScope repo, PackageNode node, Configuration config) {
        this(PackageScope.newPackage(repo, node.getSourceLoc()), node, config);
    }

    public PackageScope elaborate() {
        Identifier name = node.getPackageInfo().getIdentifier();
        Logger.startPackage(name, node.getSourceLoc());

        initializePackageDefs();
        var topNode = findTopNode();
        var topScope = scope.instantiateTop(topNode.getIdentifier());
        bindConfiguredAttributes(topNode, topScope);
        new ComponentElaborator(topScope).build(topNode);
        setSignature(topScope, topNode);

        Logger.finishPackage();
        return scope;
    }

    public static Binding evaluateBinding(BindingNode binding, ComponentScope scope) {
        Identifier id = binding.getHead().getSymbol();
        List<Identifier> freeVariables = switch (binding.getHead()) {
            case CallExpr param -> param.getArguments().stream()
                    .map(IdentifierExpr.class::cast)
                    .map(IdentifierExpr::getIdentifier).toList();
            case IdentifierExpr _ -> List.of();
        };
        var macro = Macro.of(freeVariables, binding.getExpr());

        // Check whether the macro is closed under the current scope
        var frees = FreeVariableDetector.detect(macro.expr());
        macro.identifiers().forEach(frees::remove);
        frees.remove(id);
        for (var id1 : frees) {
            if (scope.resolveIdentifier(id1) == null) {
                Logger.error("Macro {} is not closed, missing variable {}", macro.expr(), id1);
                throw new CompileCancellationException();
            }
        }

        return IdEntityMap.of(id, macro);
    }

    private void initializePackageDefs() {
        for (var element : node.getElements()) {
            switch (element) {
                case EnumNode _enum -> scope.defs().addEnum(_enum);
                case ImportNode _import -> scope.defs().addImport(_import);
                case ComponentNode _component -> scope.defs().addComponent(_component);
                case InterfaceNode _interface -> scope.defs().addInterface(_interface);
            }
        }
    }

    private ComponentNode findTopNode() {
        return node.getComponents().stream()
                .filter(node -> config.topName().equals(node.getIdentifier()))
                .findAny().orElseGet(() -> {
                    Logger.warn("No component named '{}' found, using the first component as top",
                            config.topName().name());
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
            case IdEntityMap binding -> binding.forEach(topScope::bind);
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
            var signature = node.getSignature();
            extractIOList(signature.getInput()).forEach(scope::addInput);
            extractIOList(signature.getOutput()).forEach(scope::addOutput);
        }

        private void buildInterfaceIO(InterfaceNode node) {
            var factory = new FactoryCreator(scope).createFor(node);
            var product = factory.create(scope, Identifier.of(InterfaceNode.OUTPUT_NAME));

            extractIOList(node.getSignature()).forEach(scope::addInput);
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
                case ComponentInstantiationNode instantiation -> buildComponent(instantiation);
            }
            Logger.exitLoc();
        }

        private void buildBlock(BlockNode block) {
            block.getElements().forEach(this::buildElement);
        }

        private void buildLoop(LoopNode loop) {
            var generator = loop.getGenerator();
            for (var entity : requireEList(evaluator.evaluate(generator.getFrom()))) {
                int count = scope.getBindingCount();
                scope.overrideBindings(IdEntityMap.of(generator.getHead(), entity));
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
            scope.overrideBindings(evaluateBinding(binding, scope));
        }

        private void buildData(DataInstantiationNode node) {
            var baseFactory = new FactoryCreator(scope).createFor(node);
            for (var rest : node.getDataRests()) {
                var factory = baseFactory;
                if (!rest.getDimensions().isEmpty()) {
                    var dimensions = new ArrayList<>(rest.getDimensions());
                    Collections.reverse(dimensions);
                    for (ExprNode dimension : dimensions) {
                        var size = evaluator.evaluateToInt(dimension);
                        factory = new Array.Factory(compilerOf(scope).getIndexer(), size, factory);
                    }
                }
                var id = rest.getIdentifier();
                scope.bind(id, factory.create(scope, id));
            }
        }

        private void buildComponent(ComponentInstantiationNode instantiation) {
            evaluator.evaluate(instantiation.getExpr(), instantiation.needDeclareInPlace());
        }
    }
}
