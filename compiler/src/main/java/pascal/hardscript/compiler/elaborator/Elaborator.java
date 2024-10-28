package pascal.hardscript.compiler.elaborator;

import pascal.hardscript.compiler.analysis.HierarchyAnalysis;
import pascal.hardscript.compiler.ast.DesignNode;
import pascal.hardscript.compiler.ast.Identifier;
import pascal.hardscript.compiler.ast.SourceLoc;
import pascal.hardscript.compiler.ast.module.*;
import pascal.hardscript.compiler.model.components.Component;
import pascal.hardscript.compiler.model.connection.*;
import pascal.hardscript.compiler.model.scope.*;

import java.util.LinkedList;
import java.util.List;

import static pascal.hardscript.compiler.elaborator.Types.requiresMatchedSize;

public final class Elaborator {
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
        var sourceLoc = instantiation.getSourceLoc();
        var evaluator = parent.getEvaluator();
        var parameters = evaluator.evaluateToPort(instantiation.getPort());
        var arguments = new TypeInstantiator(sourceLoc)
                .instantiate(parent, evaluator.evaluateToInstanceType(instantiation.getType()));
        requiresMatchedSize(sourceLoc, arguments.from(), parameters.from());
        requiresMatchedSize(sourceLoc, arguments.to(), parameters.to());
        for (int i = 0; i < arguments.from().size(); ++i) {
            parent.alias(parameters.from().get(i), arguments.from().get(i));
        }
        for (int i = 0; i < arguments.to().size(); ++i) {
            parent.alias(parameters.to().get(i), arguments.to().get(i));
        }
    }

    private class TypeInstantiator {
        private final SourceLoc sourceLoc;

        private TypeInstantiator(SourceLoc sourceLoc) {
            this.sourceLoc = sourceLoc;
        }

        private Port instantiate(Scope parent, InstanceType type) {
            var identifier = Identifier.of("$instantiation" + sourceLoc);
            identifier.setSourceLoc(sourceLoc);
            var instance = parent.instantiateModuleScope(identifier);
            return switch (type) {
                case AtomType atomType -> instantiateAtomType(instance, atomType);
                case CombineType combineType -> instantiateCombineType(instance, combineType);
                case ParallelType parallelType -> instantiateParallelType(instance, parallelType);
            };
        }

        private Port instantiateCombineType(Scope scope, CombineType combineType) {
            var lhs = instantiate(scope, combineType.lhs());
            var rhs = instantiate(scope, combineType.rhs());

            requiresMatchedSize(sourceLoc, lhs.to(), rhs.from());
            for (int i = 0; i < lhs.to().size(); ++i) {
                scope.alias(lhs.to().get(i), rhs.from().get(i));
            }
            return Port.of(lhs.from(), rhs.to());
        }

        private Port instantiateParallelType(Scope scope, ParallelType parallelType) {
            List<Component> input = new LinkedList<>(), output = new LinkedList<>();
            for (var sub : parallelType.types()) {
                var port = instantiate(scope, sub);
                input.addAll(port.from());
                output.addAll(port.to());
            }
            return Port.of(input, output);
        }

        private Port instantiateAtomType(Scope scope, AtomType atomType) {
            ModuleScope module;
            if (info.containsModule(atomType.identifier())) {
                var node = info.getModuleNode(atomType.identifier());
                module = scope.instantiateModuleScope(atomType.identifier());
                requiresMatchedSize(sourceLoc, node.getArguments(), atomType.parameters());
                var iterator = node.getArguments().iterator();
                for (var entity : atomType.parameters()) {
                    module.bind(iterator.next(), entity);
                }
                dispatchElementBuilder(module, node.getElements());
            } else {
                module = BuiltInModuleFactory.createBuiltInModule(scope, atomType);
            }
            return Port.of(module.getInputs(), module.getOutputs());
        }
    }
}
