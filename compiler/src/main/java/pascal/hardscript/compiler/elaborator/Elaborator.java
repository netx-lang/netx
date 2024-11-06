package pascal.hardscript.compiler.elaborator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pascal.hardscript.compiler.ast.DesignNode;
import pascal.hardscript.compiler.ast.SourceLoc;
import pascal.hardscript.compiler.ast.component.*;
import pascal.hardscript.compiler.ast.expr.ExprNode;
import pascal.hardscript.compiler.exception.CompileCancellationException;
import pascal.hardscript.compiler.model.Entity;
import pascal.hardscript.compiler.model.ListEntity;
import pascal.hardscript.compiler.model.component.AtomType;
import pascal.hardscript.compiler.model.component.CombineType;
import pascal.hardscript.compiler.model.component.ComponentType;
import pascal.hardscript.compiler.model.component.ParallelType;
import pascal.hardscript.compiler.model.instance.Instance;
import pascal.hardscript.compiler.model.scope.ComponentInfo;
import pascal.hardscript.compiler.model.scope.ComponentScope;
import pascal.hardscript.compiler.model.scope.DesignScope;
import pascal.hardscript.compiler.model.scope.Scope;
import pascal.hardscript.util.exception.ShallNotReachHereError;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

import static pascal.hardscript.compiler.elaborator.Types.requiresMatchedSize;

public final class Elaborator {
    private static final Logger log = LoggerFactory.getLogger(Elaborator.class);
    private final DesignNode node;
    private final DesignScope design;
    private final ComponentInfo info;
    private final String TOP_COMPONENT = "top";

    public Elaborator(DesignNode node) {
        this.node = node;
        this.info = ComponentInfo.of(node.getComponents());
        this.design = DesignScope.newDesign(info);
    }

    public DesignScope elaborate() {
        var topNode = node.getComponents().stream()
                .filter(n -> TOP_COMPONENT.equals(n.getIdentifier().string()))
                .findFirst().orElseGet(() -> {
                    log.warn("No component named '{}' found, using the first component as top", TOP_COMPONENT);
                    return node.getComponents().getFirst();
                });
        var scope = design.instantiateComponentScope(topNode.getIdentifier());
        new ScopeElaborator(scope).build(topNode);
        return design;
    }

    private final class ScopeElaborator {
        private final Scope scope;
        private final Evaluator evaluator;

        private ScopeElaborator(Scope scope) {
            this.scope = scope;
            this.evaluator = scope.getEvaluator();
        }

        public void build(ComponentNode node) {
            dispatchElementBuilder(node.getElements());
            var portType = makeCommonPort(node.getSourceLoc(), node.getType().getFrom(), node.getType().getTo());
            if (!(scope instanceof ComponentScope module)) {
                throw new ShallNotReachHereError();
            }
            portType.from().forEach(module::addInput);
            portType.to().forEach(module::addOutput);
        }

        public void build(LoopNode node) {
            dispatchElementBuilder(node.getElements());
        }

        private void dispatchElementBuilder(List<ElementNode> list) {
            for (var element : list) {
                switch (element) {
                    case LoopNode loop -> buildElementIntoScope(loop);
                    case BindingNode binding -> buildElementIntoScope(binding);
                    case DataInstantiationNode component -> buildElementIntoScope(component);
                    case ComponentInstantiationNode instantiation -> buildElementIntoScope(instantiation);
                }
            }
        }

        private void buildElementIntoScope(LoopNode loop) {
            var generator = loop.getGenerator();
            var id = evaluator.getIdentifierFrom(generator.getHead());
            var list = evaluator.evaluateToList(generator.getFrom());
            for (var entity : list.entities()) {
                var subScope = scope.instantiateBlockScope();
                subScope.bind(id, entity);
                new ScopeElaborator(subScope).build(loop);
            }
        }

        private void buildElementIntoScope(BindingNode binding) {
            var head = evaluator.getIdentifierFrom(binding.getHead());
            var expr = evaluator.evaluate(binding.getExpr());
            scope.bind(head, expr);
        }

        private void buildElementIntoScope(DataInstantiationNode node) {
            var factory = evaluator.evaluateToDataFactory(node.getType());
            var component = scope instanceof ComponentScope componentScope
                    ? componentScope : scope.getUpperComponent();
            node.getSymbols().stream()
                    .map(evaluator::getIdentifierFrom)
                    .forEach(id -> component.bind(id, factory.create(id)));
        }

        private void buildElementIntoScope(ComponentInstantiationNode instantiation) {
            var sourceLoc = instantiation.getSourceLoc();
            var parameters = makeCommonPort(sourceLoc, instantiation.getFrom(), instantiation.getTo());
            var arguments = new Instantiator(instantiation).instantiate(
                    evaluator.evaluateToComponentType(instantiation.getType()));

            requiresMatchedSize(sourceLoc, arguments.from(), parameters.from());
            requiresMatchedSize(sourceLoc, arguments.to(), parameters.to());
            for (int i = 0; i < arguments.from().size(); ++i) {
                scope.addAlias(parameters.from().get(i), arguments.from().get(i));
            }
            for (int i = 0; i < arguments.to().size(); ++i) {
                scope.addAlias(parameters.to().get(i), arguments.to().get(i));
            }
        }

        private ComponentPort makeCommonPort(SourceLoc sourceLoc, ExprNode from, ExprNode to) {
            return ComponentPort.of(
                    extractInstanceList(sourceLoc, evaluator.evaluate(from)),
                    extractInstanceList(sourceLoc, evaluator.evaluate(to)));
        }

        private List<Instance> extractInstanceList(SourceLoc sourceLoc, Entity entity) {
            if (entity instanceof ListEntity list) {
                if (!list.entities().isEmpty()
                        && list.entities().stream().allMatch(Predicate.not(Instance.class::isInstance))) {
                    log.error("{}: Expected list of components but got {}", sourceLoc, list);
                    throw new CompileCancellationException();
                }
                return list.entities().stream().map(Instance.class::cast).toList();
            } else if (entity instanceof Instance instance) {
                return List.of(instance);
            }
            throw new ShallNotReachHereError();
        }

        private class Instantiator {
            private final SourceLoc sourceLoc;

            private Instantiator(ComponentInstantiationNode instantiation) {
                this.sourceLoc = instantiation.getSourceLoc();
            }

            private ComponentPort instantiate(ComponentType type) {
                return switch (type) {
                    case AtomType atomType -> instantiateAtomType(atomType);
                    case CombineType combineType -> instantiateCombineType(combineType);
                    case ParallelType parallelType -> instantiateParallelType(parallelType);
                };
            }

            private ComponentPort instantiateCombineType(CombineType combineType) {
                var lhs = instantiate(combineType.lhs());
                var rhs = instantiate(combineType.rhs());

                requiresMatchedSize(sourceLoc, lhs.to(), rhs.from());
                for (int i = 0; i < lhs.to().size(); ++i) {
                    scope.addAlias(lhs.to().get(i), rhs.from().get(i));
                }
                return ComponentPort.of(lhs.from(), rhs.to());
            }

            private ComponentPort instantiateParallelType(ParallelType parallelType) {
                List<Instance> input = new LinkedList<>(), output = new LinkedList<>();
                for (var sub : parallelType.types()) {
                    var port = instantiate(sub);
                    input.addAll(port.from());
                    output.addAll(port.to());
                }
                return ComponentPort.of(input, output);
            }

            private ComponentPort instantiateAtomType(AtomType atomType) {
                ComponentScope module = info.containsComponent(atomType.identifier())
                        ? instantiateSubComponent(atomType)
                        : BuiltInComponentFactory.createBuiltInComponent(scope, atomType);
                return ComponentPort.of(module.getInputs(), module.getOutputs());
            }

            private ComponentScope instantiateSubComponent(AtomType atomType) {
                var node = info.getComponentNode(atomType.identifier());
                var componentScope = scope.instantiateComponentScope(
                        scope.getNameAllocator().allocateFor(atomType));

                requiresMatchedSize(sourceLoc, node.getArguments(), atomType.parameters());
                var iterator = node.getArguments().iterator();
                for (var entity : atomType.parameters()) {
                    componentScope.bind(iterator.next(), entity);
                }
                new ScopeElaborator(componentScope).build(node);
                return componentScope;
            }
        }
    }

    private record ComponentPort(List<Instance> from, List<Instance> to) {
        public static ComponentPort of(List<Instance> from, List<Instance> to) {
            return new ComponentPort(from, to);
        }
    }

}
