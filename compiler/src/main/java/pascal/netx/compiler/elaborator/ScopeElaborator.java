package pascal.netx.compiler.elaborator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pascal.netx.compiler.ast.element.*;
import pascal.netx.compiler.ast.expr.ExprNode;
import pascal.netx.compiler.exception.CompileCancellationException;
import pascal.netx.compiler.exception.TypeCheckingException;
import pascal.netx.compiler.io.Configuration;
import pascal.netx.compiler.model.EntityList;
import pascal.netx.compiler.model.component.*;
import pascal.netx.compiler.model.instance.Array;
import pascal.netx.compiler.model.instance.DataBundle;
import pascal.netx.compiler.model.instance.DataComposition;
import pascal.netx.compiler.model.instance.Instance;
import pascal.netx.compiler.model.instance.type.ArrayType;
import pascal.netx.compiler.model.instance.type.InstanceType;
import pascal.netx.compiler.model.instance.type.InterfaceType;
import pascal.netx.compiler.model.scope.Attributes;
import pascal.netx.compiler.model.scope.ComponentScope;
import pascal.netx.compiler.model.scope.binding.SingleBinding;
import pascal.netx.util.lang.BuiltinComponent;
import pascal.netx.util.lang.Identifier;
import pascal.netx.util.lang.SourceLoc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static pascal.netx.compiler.elaborator.Types.requiresComponentType;
import static pascal.netx.compiler.elaborator.Types.requiresMatchedSize;

final class ScopeElaborator {
    private static final Logger log = LoggerFactory.getLogger(ScopeElaborator.class);
    private final ComponentScope scope;
    private final Evaluator evaluator;

    private final Configuration config;
    private final BuiltInComponentFactory factory;

    ScopeElaborator(Configuration config, ComponentScope scope) {
        this.config = config;
        this.scope = scope;
        this.factory = scope.getGlobal().getBuiltinFactory();
        this.evaluator = scope.getEvaluator();
    }

    public void build(ComponentNode node) {
        buildComponentElements(node.getElements());
        buildComponentIO(node);
    }

    private void buildComponentIO(ComponentNode node) {
        var sourceLoc = node.getSourceLoc();
        var type = node.getType();
        extractIOList(sourceLoc, type.getInput()).forEach(scope::addInput);
        extractIOList(sourceLoc, type.getOutput()).forEach(scope::addOutput);
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

    private void buildComponentElements(List<ComponentElement> list) {
        for (var element : list) {
            switch (element) {
                case LoopNode loop -> buildLoop(loop);
                case BindingNode binding -> buildBinding(binding);
                case DataInstantiationNode data -> buildData(data);
                case ComponentInstantiationNode instantiation -> buildSubComponent(instantiation);
            }
        }
    }

    private void buildLoop(LoopNode loop) {
        var generator = loop.getGenerator();
        var id = generator.getHead();
        for (var entity : evaluator.evaluateToList(generator.getFrom()).entities()) {
            evaluator.overrideBindings(SingleBinding.of(id, entity));
            buildComponentElements(loop.getElements());
            evaluator.restoreBindings();
        }
    }

    private void buildBinding(BindingNode binding) {
        scope.bind(evaluator.evaluateBinding(binding));
    }

    private void buildData(DataInstantiationNode node) {
        var baseFactory = evaluator.evaluateToInstanceFactory(node.getType());
        switch (node.getKind()) {
            case BUNDLE -> checkFactoryLegality(node.getSourceLoc(), baseFactory.getType(), true);
            case WIRE -> checkFactoryLegality(node.getSourceLoc(), baseFactory.getType(), false);
        }
        for (var rest : node.getDataRests()) {
            var factory = baseFactory;
            if (!rest.getDimensions().isEmpty()) {
                for (ExprNode dimension : rest.getDimensions()) {
                    var size = evaluator.evaluateToInt(dimension);
                    factory = new Array.Factory(scope.getGlobal(), size, factory);
                }
            }
            var id = rest.getIdentifier();
            scope.bind(id, factory.create(scope, id));
        }
    }

    private void checkFactoryLegality(SourceLoc sourceLoc, InstanceType type, boolean isBundle) {
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
        var compType = evaluator.evaluateToComponentType(instantiation.getExpr(), inPlace);
        new Instantiator(sourceLoc).instantiate(compType);
    }

    private class Instantiator {
        private final SourceLoc sourceLoc;

        private Instantiator(SourceLoc sourceLoc) {
            this.sourceLoc = sourceLoc;
        }

        private static List<AtomType> leftMostAtomsOf(ComponentType type) {
            List<AtomType> result = new ArrayList<>();
            switch (type) {
                case ParallelType parallelType -> {
                    for (var sub : parallelType.types()) {
                        result.addAll(leftMostAtomsOf(sub));
                    }
                }
                case AtomType atomType -> result.add(atomType);
                case SequentialType sequentialType -> result.addAll(leftMostAtomsOf(sequentialType.lhs()));
            }
            return result;
        }

        private static void checkTiedType(SourceLoc sourceLoc, List<DataBundle> to, List<AtomType> from) {
            int size = requiresMatchedSize(sourceLoc, from, to);
            for (int i = 0; i < size; i++) {
                if (from.get(i) instanceof ComponentAtom atom) {
                    checkSingleTiedType(sourceLoc, to.get(i), atom);
                } else {
                    log.error("{}: Expected interface but got {}", sourceLoc, from.get(i));
                    throw new TypeCheckingException();
                }
            }
        }

        private static void checkSingleTiedType(SourceLoc sourceLoc, DataBundle lhs, ComponentAtom rhs) {
            var lhsID = lhs.getType().getId();
            var rhsID = rhs.getDefinition().getIdentifier();
            if (rhs.getDefinition().getKind() == ComponentNode.Kind.INTERFACE) {
                if (lhsID.equals(rhsID)) {
                    return;
                }
            }
            log.error("{}: Unmatched type in tied expression {} and {}", sourceLoc, lhsID, rhsID);
            throw new TypeCheckingException();
        }

        public void instantiate(ComponentType type) {
            doInstantiate(type);
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

        private Instantiator.ComponentPort instantiateSequentialType(SequentialType sequentialType) {
            // right-hand-side goes first
            var rhs = doInstantiate(sequentialType.rhs());
            var lhs = doInstantiate(sequentialType.lhs());

            List<? extends Instance> receiver;
            if (sequentialType.tied()) {
                var bundles = lhs.source().stream().map(DataBundle.class::cast).toList();
                checkTiedType(sourceLoc, bundles, leftMostAtomsOf(sequentialType.rhs()));
                receiver = bundles.stream().map(DataComposition::getElements).flatMap(Collection::stream).toList();
            } else {
                receiver = lhs.source();
            }

            int size = requiresMatchedSize(sourceLoc, receiver, rhs.target());
            for (int i = 0; i < size; ++i) {
                scope.getManager().assign(sourceLoc, receiver.get(i), rhs.target().get(i));
            }
            return ComponentPort.of(lhs.target(), rhs.source());
        }

        private Instantiator.ComponentPort instantiateParallelType(ParallelType parallelType) {
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

        private Instantiator.ComponentPort instantiateBuiltinAtom(BuiltinAtom atom) {
            var component = factory.createBuiltInComponent(scope, atom);
            if (atom.getBuiltin() == BuiltinComponent.REGISTER) {
                var clock = doInstantiate(requiresComponentType(sourceLoc, atom.getParameters().getFirst()));
                requiresMatchedSize(sourceLoc, 1, clock.target);
                component.getAttributes().addAttribute(Identifier.of("clock"), clock.target.getFirst());
            }
            return Instantiator.ComponentPort.of(component.getOutputs(), component.getInputs());
        }

        private Instantiator.ComponentPort instantiateComponentAtom(ComponentAtom atom) {
            var node = atom.getDefinition();
            ComponentScope component = switch (atom.getDefinition().getKind()) {
                case COMPONENT -> scope.instantiateComponentScope(atom.getIdentifier());
                case INTERFACE -> scope.instantiateInterfaceScope(atom.getIdentifier());
            };

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
