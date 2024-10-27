package pascal.hardscript.compiler.elaborator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pascal.hardscript.compiler.analysis.HierarchyAnalysis;
import pascal.hardscript.compiler.ast.DesignNode;
import pascal.hardscript.compiler.ast.SourceLoc;
import pascal.hardscript.compiler.ast.module.*;
import pascal.hardscript.compiler.exception.CompileCancellationException;
import pascal.hardscript.compiler.model.components.Component;
import pascal.hardscript.compiler.model.connection.port.Port;
import pascal.hardscript.compiler.model.connection.type.AtomType;
import pascal.hardscript.compiler.model.connection.type.CombineType;
import pascal.hardscript.compiler.model.connection.type.InstanceType;
import pascal.hardscript.compiler.model.connection.type.ParallelType;
import pascal.hardscript.compiler.model.scope.*;
import pascal.hardscript.util.exception.TodoException;

import java.util.LinkedList;
import java.util.List;

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
        var evaluator = parent.getEvaluator();
        var sourceLoc = instantiation.getSourceLoc();
        var from = new PortFlattener(sourceLoc).flatten(parent,
                evaluator.evaluateToPort(instantiation.getPorts()));
        var to = new TypeInstantiator(sourceLoc).instantiate(parent,
                evaluator.evaluateToInstanceType(instantiation.getType()));
        if (from.size() != to.size()) {
            throwMismatchedSize(sourceLoc, from.size(), to.size());
        }
        for (int i = 0; i < from.size(); ++i) {
            var fromPort = from.get(i);
            var toPort = to.get(i);
            if (fromPort.size() != toPort.size()) {
                throwMismatchedSize(sourceLoc, fromPort.size(), toPort.size());
            }
            for (int j = 0; j < fromPort.size(); ++j) {
                parent.addAlias(fromPort.get(j), toPort.get(j));
            }
        }
    }

    private static class PortFlattener {

        private final SourceLoc sourceLoc;

        private PortFlattener(SourceLoc sourceLoc) {
            this.sourceLoc = sourceLoc;
        }

        public List<List<Component>> flatten(Scope parent, Port port) {
            throw new TodoException(); // TODO:
        }
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
                case AtomType atomType -> instantiateAtomType(save, parent, atomType);
                case CombineType combineType -> instantiateCombineType(save, parent, combineType);
                case ParallelType parallelType -> instantiateParallelType(save, parent, parallelType);
            };
        }

        private Middleware instantiateCombineType(boolean save, Scope parent, CombineType combineType) {
            var lhs = doInstantiate(false, parent, combineType.lhs());
            var rhs = doInstantiate(false, parent, combineType.rhs());

            int middleSize = lhs.to.size();
            if (middleSize != rhs.from.size()) {
                throwMismatchedSize(sourceLoc, middleSize, rhs.from.size());
            }
            for (int i = 0; i < middleSize; ++i) {
                parent.addAlias(lhs.to.get(i), rhs.from.get(i));
            }
            if (save) {
                saved.add(lhs.to);
            }
            return new Middleware(lhs.from, rhs.to);
        }

        private Middleware instantiateParallelType(boolean save, Scope parent, ParallelType parallelType) {
            List<Component> input = new LinkedList<>(), output = new LinkedList<>();
            for (var sub : parallelType.types()) {
                var port = doInstantiate(save, parent, sub);
                input.addAll(port.from);
                output.addAll(port.to);
            }
            return new Middleware(input, output);
        }

        private Middleware instantiateAtomType(boolean save, Scope parent, AtomType atomType) {
            ModuleScope module;
            if (info.containsModule(atomType.identifier())) {
                var node = info.getModuleNode(atomType.identifier());
                module = parent.instantiateModuleScope(atomType.identifier());
                dispatchElementBuilder(module, node.getElements());
            } else {
                module = BuiltInModuleFactory.createBuiltInModule(parent, atomType);
            }
            return new Middleware(module.getInputs(), module.getOutputs());
        }

        private record Middleware(List<Component> from, List<Component> to) {

        }
    }

    private static void throwMismatchedSize(SourceLoc sourceLoc, int from, int to) {
        log.error("{}: Instantiation with mismatched component size: {} vs {}", sourceLoc, from, to);
        throw new CompileCancellationException();
    }
}
