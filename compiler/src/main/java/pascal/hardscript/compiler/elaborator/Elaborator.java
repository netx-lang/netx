package pascal.hardscript.compiler.elaborator;

import pascal.hardscript.compiler.analysis.HierarchyAnalysis;
import pascal.hardscript.compiler.ast.ASTNode;
import pascal.hardscript.compiler.ast.DesignNode;
import pascal.hardscript.compiler.ast.module.*;
import pascal.hardscript.compiler.model.circuit.ModuleEntity;
import pascal.hardscript.compiler.model.scope.BlockScope;
import pascal.hardscript.compiler.model.scope.DesignScope;
import pascal.hardscript.compiler.model.scope.ModuleScope;
import pascal.hardscript.compiler.model.scope.Scope;
import pascal.hardscript.util.exception.TodoException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Elaborator {
    private final DesignNode node;
    private final DesignScope design = DesignScope.newDesign();
    private final Map<Scope, ASTNode> origins = new HashMap<>();

    public Elaborator(DesignNode node) {
        this.node = node;
    }

    public void elaborate() {
        node.getModules().forEach(module -> {
            var id = module.getIdentifier();
            design.bind(id, ModuleEntity.of(id));
        });

        var hierarchy = new HierarchyAnalysis(node).analyze();
        hierarchy.topModules().forEach(
                module -> buildScope(instantiateModuleScopeIn(design, module)));
        throw new TodoException(); // TODO:
    }

    private void buildScope(ModuleScope scope) {
        var node = (ModuleNode) origins.get(scope);
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
        var id = evaluator.evaluateSymbol(generator.getHead());
        var list = evaluator.evaluateToValueList(generator.getFrom());
        for (var value : list.entities()) {
            var scope = instantiateBlockScopeIn(parent, loop);
            scope.bind(id, value);
            dispatchElementBuilder(scope, loop.getElements());
        }
    }

    private void buildElementIntoScope(Scope parent, ComponentNode component) {
        throw new TodoException(); // TODO:
    }

    private void buildElementIntoScope(Scope parent, InstantiationNode instantiation) {
        throw new TodoException(); // TODO:
    }

    private ModuleScope instantiateModuleScopeIn(Scope parent, ModuleNode module) {
        var scope = parent.instantiateModuleScope(module.getIdentifier());
        origins.put(scope, module);
        return scope;
    }

    private BlockScope instantiateBlockScopeIn(Scope parent, LoopNode loop) {
        var scope = parent.instantiateBlockScope();
        origins.put(scope, loop);
        return scope;
    }
}
