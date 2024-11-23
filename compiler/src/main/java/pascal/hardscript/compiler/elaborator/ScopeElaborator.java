package pascal.hardscript.compiler.elaborator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pascal.hardscript.compiler.ast.component.*;
import pascal.hardscript.compiler.ast.expr.ExprNode;
import pascal.hardscript.compiler.exception.CompileCancellationException;
import pascal.hardscript.compiler.io.Configuration;
import pascal.hardscript.compiler.model.EntityList;
import pascal.hardscript.compiler.model.component.*;
import pascal.hardscript.compiler.model.instance.Instance;
import pascal.hardscript.compiler.model.instance.MutableInstance;
import pascal.hardscript.compiler.model.scope.Attributes;
import pascal.hardscript.compiler.model.scope.ComponentScope;
import pascal.hardscript.compiler.model.scope.Scope;
import pascal.hardscript.util.lang.BuiltinComponent;
import pascal.hardscript.util.lang.Identifier;
import pascal.hardscript.util.lang.SourceLoc;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static pascal.hardscript.compiler.elaborator.Types.*;

final class ScopeElaborator {
    private static final Logger log = LoggerFactory.getLogger(ScopeElaborator.class);
    private final BuiltInComponentFactory factory;
    private final Configuration config;
    private final Evaluator evaluator;
    private final Scope scope;

    ScopeElaborator(Configuration config, Scope scope) {
        this.config = config;
        this.scope = scope;
        this.factory = scope.getGlobal().getBuiltinFactory();
        this.evaluator = scope.getEvaluator();
    }

    public void build(ComponentNode node) {
        dispatchElementBuilder(node.getElements());
        var component = (ComponentScope) scope;
        var sourceLoc = node.getSourceLoc();
        extractIOList(sourceLoc, node.getType().getInput()).forEach(component::addInput);
        extractIOList(sourceLoc, node.getType().getOutput()).forEach(component::addOutput);
    }

    private List<Instance> extractIOList(SourceLoc sourceLoc, ExprNode node) {
        var entity = evaluator.evaluate(node);
        if (entity instanceof EntityList(var list)) {
            List<Instance> result = new ArrayList<>();
            for (var member : list) {
                if (member instanceof Instance instance) {
                    result.add(instance);
                } else {
                    log.error("{}: Expected list of instances but got {}", sourceLoc, list);
                    throw new CompileCancellationException();
                }
            }
            return result;
        } else if (entity instanceof Instance instance) {
            return List.of(instance);
        }
        log.error("{}: Expected list of instances but got {}", sourceLoc, entity);
        throw new CompileCancellationException();
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
            new ScopeElaborator(config, subScope).build(loop);
        }
    }

    private void buildElement(BindingNode binding) {
        scope.bind(evaluator.evaluateBinding(binding));
    }

    private void buildElement(DataInstantiationNode node) {
        var factory = evaluator.evaluateToDataFactory(node.getType());
        var component = scope.getLeastUpperComponent();
        node.getSymbols().stream()
                .map(evaluator::getIdentifierFrom)
                .forEach(id -> component.bind(id, factory.create(id)));
    }

    private void buildElement(ComponentInstantiationNode instantiation) {
        var sourceLoc = instantiation.getSourceLoc();
        var compType = evaluator.evaluateToComponentType(instantiation.getRhs());

        var component = scope.getLeastUpperComponent();
        var target = extractComponentLhs(sourceLoc, instantiation.getLhs(), instantiation.needDeclareInPlace());
        var source = new Instantiator(sourceLoc).instantiate(compType);

        checkAttributeWrite(component, target, sourceLoc);
        int size = requiresMatchedSize(sourceLoc, target, source);
        for (int i = 0; i < size; i++) {
            scope.addAlias(target.get(i), source.get(i));
        }
    }

    private List<MutableInstance> extractComponentLhs(SourceLoc sourceLoc, ExprNode node, boolean declareInPlace) {
        var entity = declareInPlace ? evaluator.evaluateAndDeclInPlace(node) : evaluator.evaluate(node);
        if (entity instanceof EntityList(var list)) {
            return list.stream().map(o -> requiresMutableInstance(sourceLoc, o)).toList();
        } else {
            return List.of(requiresMutableInstance(sourceLoc, entity));
        }
    }

    private void checkAttributeWrite(ComponentScope component, List<MutableInstance> target, SourceLoc sourceLoc) {
        if (component.getAttributes() != null) {
            for (var inst : target) {
                if (component.getAttributes().contains(inst)) {
                    log.error("{}: Component Attributes {} is read-only ", sourceLoc, inst);
                    throw new CompileCancellationException();
                }
            }
        }
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

        Instantiator.ComponentPort doInstantiate(ComponentType type) {
            return switch (type) {
                case BuiltinAtom atom -> instantiateBuiltinAtom(atom);
                case InstanceAtom atom -> instantiateInstanceAtom(atom);
                case ComponentAtom atom -> instantiateComponentAtom(atom);
                case ParallelType parallelType -> instantiateParallelType(parallelType);
                case SequentialType sequentialType -> instantiateSequentialType(sequentialType);
            };
        }

        public Instantiator.ComponentPort instantiateSequentialType(SequentialType sequentialType) {
            // right-hand-side goes first
            var rhs = doInstantiate(sequentialType.rhs());
            var lhs = doInstantiate(sequentialType.lhs());

            int size = requiresMatchedSize(sourceLoc, lhs.source(), rhs.target());
            for (int i = 0; i < size; ++i) {
                scope.addAlias(lhs.source().get(i), rhs.target().get(i));
            }
            return Instantiator.ComponentPort.of(lhs.target(), rhs.source());
        }

        public Instantiator.ComponentPort instantiateParallelType(ParallelType parallelType) {
            List<Instance> input = new LinkedList<>(), output = new LinkedList<>();
            for (var sub : parallelType.types()) {
                var port = doInstantiate(sub);
                input.addAll(port.target());
                output.addAll(port.source());
            }
            return Instantiator.ComponentPort.of(input, output);
        }

        private ComponentPort instantiateInstanceAtom(InstanceAtom atom) {
            var inst = List.of(atom.getInstance());
            return switch (atom.getMode()) {
                case SOURCE -> ComponentPort.of(inst, List.of());
                case SINK -> ComponentPort.of(List.of(), inst);
                case BIDI -> ComponentPort.of(inst, inst);
            };
        }

        public Instantiator.ComponentPort instantiateBuiltinAtom(BuiltinAtom atom) {
            var component = factory.createBuiltInComponent(scope, atom);
            if (atom.getBuiltin() == BuiltinComponent.REGISTER) {
                var clock = instantiate(requiresComponentType(sourceLoc, atom.getParameters().getFirst()));
                requiresMatchedSize(sourceLoc, 1, clock);
                component.getAttributes().addAttribute(Identifier.of("clock"), clock.getFirst());
            }
            return Instantiator.ComponentPort.of(component.getOutputs(), component.getInputs());
        }

        public Instantiator.ComponentPort instantiateComponentAtom(ComponentAtom atom) {
            var node = atom.getDefinition();
            var component = scope.instantiateComponentScope(atom.getIdentifier());

            var attributes = extractAttributes(atom, node);
            for (var entry : attributes.entrySet()) {
                component.bind(entry.getKey(), entry.getValue());
            }
            component.setAttributes(attributes);
            new ScopeElaborator(config, component).build(node);
            if (!config.fullyExpand()) {
                setSignatureFor(atom, component);
            }
            return Instantiator.ComponentPort.of(component.getOutputs(), component.getInputs());
        }

        private void setSignatureFor(ComponentAtom atom, ComponentScope scope) {
            var signature = Signature.of(atom, scope.getInputs(), scope.getOutputs());
            scope.setSignature(signature);
        }

        private Attributes extractAttributes(ComponentAtom atom, ComponentNode node) {
            Attributes attributes = new Attributes();
            int size = requiresMatchedSize(sourceLoc, node.getAttributes(), atom.getParameters());
            for (int i = 0; i < size; i++) {
                var id = node.getAttributes().get(i);
                var entity = atom.getParameters().get(i);
                attributes.addAttribute(id, entity);
            }
            return attributes;
        }

        private record ComponentPort(List<Instance> target, List<Instance> source) {
            public static Instantiator.ComponentPort of(List<Instance> target, List<Instance> source) {
                return new Instantiator.ComponentPort(target, source);
            }
        }
    }
}
