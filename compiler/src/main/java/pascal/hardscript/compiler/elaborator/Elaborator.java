package pascal.hardscript.compiler.elaborator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pascal.hardscript.compiler.ast.DesignNode;
import pascal.hardscript.compiler.ast.SourceLoc;
import pascal.hardscript.compiler.ast.component.*;
import pascal.hardscript.compiler.model.component.*;
import pascal.hardscript.compiler.model.data.Data;
import pascal.hardscript.compiler.model.scope.*;
import pascal.hardscript.util.exception.ShallNotReachHereError;

import java.util.LinkedList;
import java.util.List;

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
            if (node.getType() != null) {
                var portType = evaluator.evaluateToComponentPort(node.getType());
                if (!(scope instanceof ComponentScope module)) {
                    throw new ShallNotReachHereError();
                }
                portType.from().forEach(module::addInput);
                portType.to().forEach(module::addOutput);
            }
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
            node.getSymbols().stream()
                    .map(evaluator::getIdentifierFrom)
                    .forEach(id -> scope.bind(id, factory.create(id)));
        }

        private void buildElementIntoScope(ComponentInstantiationNode instantiation) {
            var sourceLoc = instantiation.getSourceLoc();
            var parameters = evaluator.evaluateToComponentPort(instantiation.getPort());
            var arguments = new Instantiator(instantiation).instantiate(
                    evaluator.evaluateToComponentType(instantiation.getType()));

            requiresMatchedSize(sourceLoc, arguments.from(), parameters.from());
            requiresMatchedSize(sourceLoc, arguments.to(), parameters.to());
            for (int i = 0; i < arguments.from().size(); ++i) {
                scope.alias(parameters.from().get(i), arguments.from().get(i));
            }
            for (int i = 0; i < arguments.to().size(); ++i) {
                scope.alias(parameters.to().get(i), arguments.to().get(i));
            }
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
                    scope.alias(lhs.to().get(i), rhs.from().get(i));
                }
                return ComponentPort.of(lhs.from(), rhs.to());
            }

            private ComponentPort instantiateParallelType(ParallelType parallelType) {
                List<Data> input = new LinkedList<>(), output = new LinkedList<>();
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
}
