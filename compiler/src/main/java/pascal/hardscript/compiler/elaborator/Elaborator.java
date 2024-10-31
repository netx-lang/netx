package pascal.hardscript.compiler.elaborator;

import pascal.hardscript.compiler.analysis.HierarchyAnalysis;
import pascal.hardscript.compiler.ast.DesignNode;
import pascal.hardscript.compiler.ast.SourceLoc;
import pascal.hardscript.compiler.ast.module.Binding;
import pascal.hardscript.compiler.ast.module.*;
import pascal.hardscript.compiler.model.components.Component;
import pascal.hardscript.compiler.model.connection.*;
import pascal.hardscript.compiler.model.scope.*;
import pascal.hardscript.util.exception.ShallNotReachHereError;

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
        for (var node : hierarchy.topModules()) {
            var scope = design.instantiateModuleScope(node.getIdentifier());
            new ScopeElaborator(scope).build(node);
        }
        return design;
    }

    private final class ScopeElaborator {
        private final Scope scope;
        private final Evaluator evaluator;

        private ScopeElaborator(Scope scope) {
            this.scope = scope;
            this.evaluator = scope.getEvaluator();
        }

        public void build(ModuleNode node) {
            dispatchElementBuilder(node.getElements());
            var portType = evaluator.evaluateToPort(node.getType());
            if (!(scope instanceof ModuleScope module)) {
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
                    case Binding binding -> buildElementIntoScope(binding);
                    case ComponentNode component -> buildElementIntoScope(component);
                    case InstantiationNode instantiation -> buildElementIntoScope(instantiation);
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

        private void buildElementIntoScope(Binding binding) {
            var head = evaluator.getIdentifierFrom(binding.getHead());
            var expr = evaluator.evaluate(binding.getExpr());
            scope.bind(head, expr);
        }

        private void buildElementIntoScope(ComponentNode node) {
            var factory = evaluator.evaluateToComponentFactory(node.getType());
            node.getSymbols().stream()
                    .map(evaluator::getIdentifierFrom)
                    .forEach(id -> scope.bind(id, factory.create(id)));
        }

        private void buildElementIntoScope(InstantiationNode instantiation) {
            var sourceLoc = instantiation.getSourceLoc();
            var parameters = evaluator.evaluateToPort(instantiation.getPort());
            var arguments = new Instantiator(instantiation).instantiate(
                    evaluator.evaluateToInstanceType(instantiation.getType()));

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

            private Instantiator(InstantiationNode instantiation) {
                this.sourceLoc = instantiation.getSourceLoc();
            }

            private Port instantiate(InstanceType type) {
                return switch (type) {
                    case AtomType atomType -> instantiateAtomType(atomType);
                    case CombineType combineType -> instantiateCombineType(combineType);
                    case ParallelType parallelType -> instantiateParallelType(parallelType);
                };
            }

            private Port instantiateCombineType(CombineType combineType) {
                var lhs = instantiate(combineType.lhs());
                var rhs = instantiate(combineType.rhs());

                requiresMatchedSize(sourceLoc, lhs.to(), rhs.from());
                for (int i = 0; i < lhs.to().size(); ++i) {
                    scope.alias(lhs.to().get(i), rhs.from().get(i));
                }
                return Port.of(lhs.from(), rhs.to());
            }

            private Port instantiateParallelType(ParallelType parallelType) {
                List<Component> input = new LinkedList<>(), output = new LinkedList<>();
                for (var sub : parallelType.types()) {
                    var port = instantiate(sub);
                    input.addAll(port.from());
                    output.addAll(port.to());
                }
                return Port.of(input, output);
            }

            private Port instantiateAtomType(AtomType atomType) {
                ModuleScope module = info.containsModule(atomType.identifier())
                        ? instantiateSubModule(atomType)
                        : BuiltInModuleFactory.createBuiltInModule(scope, atomType);
                return Port.of(module.getInputs(), module.getOutputs());
            }

            private ModuleScope instantiateSubModule(AtomType atomType) {
                var node = info.getModuleNode(atomType.identifier());
                var moduleScope = scope.instantiateModuleScope(
                        scope.getNameAllocator().allocateFor(atomType));

                requiresMatchedSize(sourceLoc, node.getArguments(), atomType.parameters());
                var iterator = node.getArguments().iterator();
                for (var entity : atomType.parameters()) {
                    moduleScope.bind(iterator.next(), entity);
                }
                new ScopeElaborator(moduleScope).build(node);
                return moduleScope;
            }
        }
    }
}
