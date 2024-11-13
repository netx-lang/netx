package pascal.hardscript.compiler.elaborator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pascal.hardscript.compiler.ast.DesignNode;
import pascal.hardscript.compiler.ast.component.*;
import pascal.hardscript.compiler.ast.expr.ExprNode;
import pascal.hardscript.compiler.exception.CompileCancellationException;
import pascal.hardscript.compiler.model.EntityList;
import pascal.hardscript.compiler.model.component.*;
import pascal.hardscript.compiler.model.instance.Instance;
import pascal.hardscript.compiler.model.scope.Attributes;
import pascal.hardscript.compiler.model.scope.ComponentScope;
import pascal.hardscript.compiler.model.scope.DesignScope;
import pascal.hardscript.compiler.model.scope.Scope;
import pascal.hardscript.util.exception.ShallNotReachHereError;
import pascal.hardscript.util.lang.SourceLoc;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static pascal.hardscript.compiler.Configuration.TOP_COMPONENT;
import static pascal.hardscript.compiler.elaborator.Types.requiresMatchedSize;

public final class Elaborator {
    private static final Logger log = LoggerFactory.getLogger(Elaborator.class);

    private final DesignNode node;
    private final DesignScope design;

    public Elaborator(DesignNode node) {
        this.node = node;
        this.design = DesignScope.newDesign(node);
        Evaluator.setGlobal(design);
    }

    public DesignScope elaborate() {
        var topNode = findTopNode();
        var topScope = design.instantiateTop(topNode.getIdentifier());
        new ScopeElaborator(topScope).build(topNode);
        return design;
    }

    private ComponentNode findTopNode() {
        for (var component : node.getComponents()) {
            if (TOP_COMPONENT.equals(component.getIdentifier().name())) {
                return component;
            }
        }
        log.warn("No component named '{}' found, using the first component as top", TOP_COMPONENT);
        return node.getComponents().getFirst();
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
            var component = (ComponentScope) scope;
            var sourceLoc = node.getSourceLoc();
            extractInstanceList(sourceLoc, node.getType().getInput()).forEach(component::addInput);
            extractInstanceList(sourceLoc, node.getType().getOutput()).forEach(component::addOutput);
        }

        public void build(LoopNode node) {
            dispatchElementBuilder(node.getElements());
        }

        private void dispatchElementBuilder(List<ElementNode> list) {
            for (var element : list) {
                switch (element) {
                    case LoopNode loop -> buildElement(loop);
                    case BindingNode binding -> buildElement(binding);
                    case DataInstantiationNode component -> buildElement(component);
                    case ComponentInstantiationNode instantiation -> buildElement(instantiation);
                }
            }
        }

        private void buildElement(LoopNode loop) {
            var generator = loop.getGenerator();
            var id = evaluator.getIdentifierFrom(generator.getHead());
            for (var entity : evaluator.evaluateToList(generator.getFrom()).entities()) {
                var subScope = scope.instantiateBlockScope();
                subScope.bind(id, entity);
                new ScopeElaborator(subScope).build(loop);
            }
        }

        private void buildElement(BindingNode binding) {
            var head = evaluator.getIdentifierFrom(binding.getHead());
            var expr = evaluator.evaluate(binding.getExpr());
            scope.bind(head, expr);
        }

        private void buildElement(DataInstantiationNode node) {
            var factory = evaluator.evaluateToDataFactory(node.getType());
            var component = factory.getParent();
            node.getSymbols().stream()
                    .map(evaluator::getIdentifierFrom)
                    .forEach(id -> component.bind(id, factory.create(id)));
        }

        private void buildElement(ComponentInstantiationNode instantiation) {
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
            if (entity instanceof EntityList list) {
                List<Instance> result = new ArrayList<>();
                for (var member : list.entities()) {
                    if (member instanceof Instance instance) {
                        result.add(instance);
                    } else {
                        log.error("{}: Expected list of components but got {}", sourceLoc, list);
                        throw new CompileCancellationException();
                    }
                }
                return result;
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
                    case BuiltinAtom atom -> instantiateBuiltinAtom(atom);
                    case ComponentAtom atom -> instantiateComponentAtom(atom);
                    case ParallelType parallelType -> instantiateParallelType(parallelType);
                    case SequentialType sequentialType -> instantiateSequentialType(sequentialType);
                };
            }

            public ComponentPort instantiateSequentialType(SequentialType sequentialType) {
                // right-hand-side goes first
                var rhs = doInstantiate(sequentialType.rhs());
                var lhs = doInstantiate(sequentialType.lhs());

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

            public ComponentPort instantiateBuiltinAtom(BuiltinAtom atom) {
                var component = design.getBuiltinFactory().createBuiltInComponent(scope, atom);
                component.setSignature(atom);
                return ComponentPort.of(component.getOutputs(), component.getInputs());
            }

            public ComponentPort instantiateComponentAtom(ComponentAtom atom) {
                var node = atom.getDefinition();
                var component = scope.instantiateComponentScope(node.getIdentifier());
                var attributes = extractAttributes(atom, node);
                for (var entry : attributes.entrySet()) {
                    component.bind(entry.getKey(), entry.getValue());
                }
                component.setSignature(atom);
                component.setAttributes(attributes);
                new ScopeElaborator(component).build(node);
                return ComponentPort.of(component.getOutputs(), component.getInputs());
            }

            private Attributes extractAttributes(ComponentAtom atom, ComponentNode node) {
                Attributes attributes = new Attributes();
                requiresMatchedSize(sourceLoc, node.getAttributes(), atom.getParameters());
                var iterator = node.getAttributes().iterator();
                for (var entity : atom.getParameters()) {
                    if (!(entity instanceof Instance instance)) {
                        log.error("{}: Invalid component attribute {}", sourceLoc, entity);
                        throw new CompileCancellationException();
                    }
                    var identifier = iterator.next();
                    attributes.addAttribute(identifier, instance);
                }
                return attributes;
            }
        }

        private record ComponentPort(List<Instance> target, List<Instance> source) {
            public static ComponentPort of(List<Instance> target, List<Instance> source) {
                return new ComponentPort(target, source);
            }
        }
    }
}
