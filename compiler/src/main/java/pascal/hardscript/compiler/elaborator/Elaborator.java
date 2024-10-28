package pascal.hardscript.compiler.elaborator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pascal.hardscript.compiler.analysis.HierarchyAnalysis;
import pascal.hardscript.compiler.ast.DesignNode;
import pascal.hardscript.compiler.ast.SourceLoc;
import pascal.hardscript.compiler.ast.module.*;
import pascal.hardscript.compiler.exception.CompileCancellationException;
import pascal.hardscript.compiler.model.components.Component;
import pascal.hardscript.compiler.model.connection.port.AtomPort;
import pascal.hardscript.compiler.model.connection.port.ClosurePort;
import pascal.hardscript.compiler.model.connection.port.CommonPort;
import pascal.hardscript.compiler.model.connection.port.MapstoPort;
import pascal.hardscript.compiler.model.connection.type.AtomType;
import pascal.hardscript.compiler.model.connection.type.CombineType;
import pascal.hardscript.compiler.model.connection.type.InstanceType;
import pascal.hardscript.compiler.model.connection.type.ParallelType;
import pascal.hardscript.compiler.model.scope.*;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;

public final class Elaborator {
    private static final Logger log = LoggerFactory.getLogger(Elaborator.class);
    private final DesignNode node;
    private final ModuleInfo info;
    private final DesignScope design;

    public Elaborator(DesignNode node) {
        this.node = node;
        this.info = ModuleInfo.of(node.getModules());
        this.design = DesignScope.newDesign(info);
    }

    public DesignScope elaborate() {
        var hierarchy = new HierarchyAnalysis(node).analyze();
        hierarchy.topModules().forEach(this::buildScope);
        return design;
    }

    private void buildScope(ModuleNode node) {
        var scope = design.instantiateModuleScope(node.getIdentifier());
        var list = node.getElements();
        dispatchElementBuilder(scope, list);
    }

    private void dispatchElementBuilder(Scope scope, List<ElementNode> list) {
        for (var element : list) {
            switch (element) {
                case LoopNode loop -> buildElementIntoScope(scope, loop);
                case ComponentNode component -> buildElementIntoScope(scope, component);
                case InstantiationNode instantiation -> buildElementIntoScope(scope, instantiation);
            }
        }
    }

    private void buildElementIntoScope(Scope parent, LoopNode loop) {
        var evaluator = parent.getEvaluator();
        var generator = loop.getGenerator();
        var id = evaluator.getIdentifierFrom(generator.getHead());
        var list = evaluator.evaluateToList(generator.getFrom());
        for (var entity : list.entities()) {
            var scope = parent.instantiateBlockScope();
            scope.bind(id, entity);
            dispatchElementBuilder(scope, loop.getElements());
        }
    }

    private void buildElementIntoScope(Scope parent, ComponentNode node) {
        var evaluator = parent.getEvaluator();
        var factory = evaluator.evaluateToComponentFactory(node.getType());
        node.getSymbols().stream()
                .map(evaluator::getIdentifierFrom)
                .forEach(id -> parent.bind(id, factory.create(id)));
    }

    private void buildElementIntoScope(Scope parent, InstantiationNode instantiation) {
        FlattenedInstantiation result = flatten(parent, instantiation);
        for (int i = 0; i < result.size(); ++i) {
            parent.alias(result.parameters.get(i), result.arguments.get(i));
        }
    }

    private FlattenedInstantiation flatten(Scope parent, InstantiationNode instantiation) {
        var evaluator = parent.getEvaluator();
        var sourceLoc = instantiation.getSourceLoc();
        var arguments = new TypeInstantiator(sourceLoc).instantiate(parent,
                evaluator.evaluateToInstanceType(instantiation.getType()));
        var port = evaluator.evaluateToPort(instantiation.getPorts());
        List<List<Component>> parameters = switch (port) {
            case CommonPort common -> flatten(common);
            case ClosurePort closure -> {
                List<List<Component>> result = new LinkedList<>();
                var beginning = flatten(closure.lhs());
                var ending = flatten(closure.rhs());
                arguments = arguments.subList(0, beginning.size());
                arguments = arguments.subList(arguments.size() - ending.size(), arguments.size());
                result.addAll(beginning);
                result.addAll(ending);
                yield result;
            }
        };
        return FlattenedInstantiation.of(sourceLoc, parameters, arguments);
    }

    private record FlattenedInstantiation(List<Component> parameters, List<Component> arguments) {
        static FlattenedInstantiation of(SourceLoc sourceLoc,
                                         List<List<Component>> parameters,
                                         List<List<Component>> arguments) {
            throwIfSizeMismatched(sourceLoc, arguments, parameters);
            var mismatched = IntStream.range(0, arguments.size())
                    .filter(i -> arguments.get(i).size() != parameters.get(i).size())
                    .findFirst();
            if (mismatched.isPresent()) {
                var index = mismatched.getAsInt();
                throwIfSizeMismatched(sourceLoc, arguments.get(index), parameters.get(index));
            }
            return new FlattenedInstantiation(
                    parameters.stream().flatMap(List::stream).toList(),
                    arguments.stream().flatMap(List::stream).toList());
        }

        int size() {
            return parameters.size();
        }
    }

    public List<List<Component>> flatten(CommonPort port) {
        return switch (port) {
            case MapstoPort mapsto -> {
                var result = new LinkedList<List<Component>>();
                result.addAll(flatten(mapsto.from()));
                result.addAll(flatten(mapsto.to()));
                yield result;
            }
            case AtomPort atom -> List.of(List.copyOf(atom.components()));
        };
    }

    private class TypeInstantiator {
        private final SourceLoc sourceLoc;
        private final List<List<Component>> saved = new LinkedList<>();

        private TypeInstantiator(SourceLoc sourceLoc) {
            this.sourceLoc = sourceLoc;
        }

        public List<List<Component>> instantiate(Scope parent, InstanceType type) {
            // FIXME: check the associativity and order
            var middleware = doInstantiate(true, parent, type);
            saved.addFirst(middleware.from);
            saved.addLast(middleware.to);
            return saved;
        }

        private Middleware doInstantiate(boolean save, Scope parent, InstanceType type) {
            return switch (type) {
                case AtomType atomType -> instantiateAtomType(parent, atomType);
                case CombineType combineType -> instantiateCombineType(save, parent, combineType);
                case ParallelType parallelType -> instantiateParallelType(parent, parallelType);
            };
        }

        private Middleware instantiateCombineType(boolean save, Scope parent, CombineType combineType) {
            var lhs = doInstantiate(save, parent, combineType.lhs());
            var rhs = doInstantiate(save, parent, combineType.rhs());

            throwIfSizeMismatched(sourceLoc, lhs.to, rhs.from);
            for (int i = 0; i < lhs.to.size(); ++i) {
                parent.alias(lhs.to.get(i), rhs.from.get(i));
            }
            if (save) {
                saved.add(lhs.to);
            }
            return new Middleware(lhs.from, rhs.to);
        }

        private Middleware instantiateParallelType(Scope parent, ParallelType parallelType) {
            List<Component> input = new LinkedList<>(), output = new LinkedList<>();
            for (var sub : parallelType.types()) {
                var port = doInstantiate(false, parent, sub);
                input.addAll(port.from);
                output.addAll(port.to);
            }
            return new Middleware(input, output);
        }

        private Middleware instantiateAtomType(Scope parent, AtomType atomType) {
            ModuleScope module;
            if (info.containsModule(atomType.identifier())) {
                var node = info.getModuleNode(atomType.identifier());
                module = parent.instantiateModuleScope(atomType.identifier());
                throwIfSizeMismatched(sourceLoc, node.getArguments(), atomType.parameters());
                var iterator = node.getArguments().iterator();
                for (var entity : atomType.parameters()) {
                    module.bind(iterator.next(), entity);
                }
                dispatchElementBuilder(module, node.getElements());
            } else {
                module = BuiltInModuleFactory.createBuiltInModule(parent, atomType);
            }
            return new Middleware(module.getInputs(), module.getOutputs());
        }

        private record Middleware(List<Component> from, List<Component> to) {

        }
    }

    private static void throwIfSizeMismatched(SourceLoc sourceLoc, List<?> from, List<?> to) {
        if (from.size() != to.size()) {
            log.error("{}: Instantiation with mismatched component size: {} vs {}", sourceLoc, from.size(), to.size());
            throw new CompileCancellationException();
        }
    }
}
