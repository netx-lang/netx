package pascal.hardscript.compiler.elaborator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pascal.hardscript.compiler.ast.DesignNode;
import pascal.hardscript.compiler.ast.SourceLoc;
import pascal.hardscript.compiler.ast.component.*;
import pascal.hardscript.compiler.ast.expr.ExprNode;
import pascal.hardscript.compiler.exception.CompileCancellationException;
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
            var module = (ComponentScope) scope;
            var sourceLoc = node.getSourceLoc();
            extractInstanceList(sourceLoc, node.getType().getFrom()).forEach(module::addInput);
            extractInstanceList(sourceLoc, node.getType().getTo()).forEach(module::addOutput);

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
            var compType = evaluator.evaluateToComponentType(instantiation.getRhs());

            var target = extractInstanceList(sourceLoc, instantiation.getLhs());
            var source = new Instantiator(sourceLoc).instantiate(compType);

            requiresMatchedSize(sourceLoc, target, source);
            for (int i = 0; i < target.size(); i++) {
                scope.addAlias(source.get(i), target.get(i));
            }
        }

        private List<Instance> extractInstanceList(SourceLoc sourceLoc, ExprNode node) {
            var entity = evaluator.evaluate(node);
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

            private Instantiator(SourceLoc sourceLoc) {
                this.sourceLoc = sourceLoc;
            }

            public List<Instance> instantiate(ComponentType type) {
                var result = doInstantiate(type);
                if (!result.source().isEmpty()) {
                    log.error("{}: Incomplete component instantiation {}", sourceLoc, type);
                    throw new CompileCancellationException();
                }
                return result.target();
            }

            ComponentPort doInstantiate(ComponentType type) {
                return switch (type) {
                    case AtomType atomType -> instantiateAtomType(atomType);
                    case CombineType combineType -> instantiateCombineType(combineType);
                    case ParallelType parallelType -> instantiateParallelType(parallelType);
                };
            }

            public ComponentPort instantiateCombineType(CombineType combineType) {
                var lhs = doInstantiate(combineType.lhs());
                var rhs = doInstantiate(combineType.rhs());

                requiresMatchedSize(sourceLoc, lhs.source(), rhs.target());
                for (int i = 0; i < lhs.source().size(); ++i) {
                    scope.addAlias(rhs.target().get(i), lhs.source().get(i));
                }
                return ComponentPort.of(lhs.target(), rhs.source());
            }

            public ComponentPort instantiateParallelType(ParallelType parallelType) {
                List<Instance> input = new LinkedList<>(), output = new LinkedList<>();
                for (var sub : parallelType.types()) {
                    var port = doInstantiate(sub);
                    input.addAll(port.target());
                    output.addAll(port.source());
                }
                return ComponentPort.of(input, output);
            }

            public ComponentPort instantiateAtomType(AtomType atomType) {
                ComponentScope module = info.containsComponent(atomType.identifier())
                        ? instantiateSubComponent(atomType)
                        : BuiltInComponentFactory.createBuiltInComponent(scope, atomType);
                return ComponentPort.of(module.getOutputs(), module.getInputs());
            }

            public ComponentScope instantiateSubComponent(AtomType atomType) {
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

        private record ComponentPort(List<Instance> target, List<Instance> source) {
            public static ComponentPort of(List<Instance> target, List<Instance> source) {
                return new ComponentPort(target, source);
            }
        }
    }
}
