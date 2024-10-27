package pascal.hardscript.compiler.elaborator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pascal.hardscript.compiler.analysis.HierarchyAnalysis;
import pascal.hardscript.compiler.ast.DesignNode;
import pascal.hardscript.compiler.ast.module.*;
import pascal.hardscript.compiler.model.connection.Connection;
import pascal.hardscript.compiler.model.scope.DesignScope;
import pascal.hardscript.compiler.model.scope.ModuleInfo;
import pascal.hardscript.compiler.model.scope.Scope;

import java.util.List;

public final class Elaborator {
    private static final Logger log = LoggerFactory.getLogger(Elaborator.class);
    private final DesignNode node;
    private final DesignScope design;

    public Elaborator(DesignNode node) {
        this.node = node;
        design = DesignScope.newDesign(ModuleInfo.of(
                node.getModules().stream().map(ModuleNode::getIdentifier).toList()));
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
        var type = evaluator.evaluateToInstanceType(instantiation.getType());
        var port = evaluator.evaluateToPort(instantiation.getConnection());
        parent.addConnection(Connection.of(type, port));
    }

}
