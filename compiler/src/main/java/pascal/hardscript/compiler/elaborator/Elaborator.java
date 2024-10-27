package pascal.hardscript.compiler.elaborator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pascal.hardscript.compiler.analysis.HierarchyAnalysis;
import pascal.hardscript.compiler.ast.DesignNode;
import pascal.hardscript.compiler.ast.Identifier;
import pascal.hardscript.compiler.ast.SourceLoc;
import pascal.hardscript.compiler.ast.module.*;
import pascal.hardscript.compiler.exception.CompileCancellationException;
import pascal.hardscript.compiler.model.ParameteriezedEntity;
import pascal.hardscript.compiler.model.circuit.Clock;
import pascal.hardscript.compiler.model.circuit.Component;
import pascal.hardscript.compiler.model.circuit.Register;
import pascal.hardscript.compiler.model.circuit.Wire;
import pascal.hardscript.compiler.model.scope.DesignScope;
import pascal.hardscript.compiler.model.scope.Scope;
import pascal.hardscript.util.exception.TodoException;

import java.util.List;
import java.util.Set;

public final class Elaborator {
    private static final Logger log = LoggerFactory.getLogger(Elaborator.class);
    private final DesignNode node;
    private final DesignScope design = DesignScope.newDesign();
    private static final Set<String> BUILTIN = Set.of(
            "wire", "reg", "clock", "bit", "trigger_on", "reset_on", "pos", "neg");

    public Elaborator(DesignNode node) {
        this.node = node;
        initialize();
    }

    public DesignScope elaborate() {
        var hierarchy = new HierarchyAnalysis(node).analyze();
        hierarchy.topModules().forEach(this::buildScope);
        return design;
    }

    private void initialize() {
        bindBuiltIn();
        bindModule();
    }

    private void bindModule() {
        node.getModules().forEach(module -> {
            var id = module.getIdentifier();
            design.bind(id, ParameteriezedEntity.of(id));
        });
    }

    private void bindBuiltIn() {
        BUILTIN.stream().map(Identifier::of)
                .forEach(id -> design.bind(id, ParameteriezedEntity.of(id)));
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
        var id = evaluator.evaluateToIdentifier(generator.getHead());
        var list = evaluator.evaluateToValueList(generator.getFrom());
        for (var value : list.entities()) {
            var scope = parent.instantiateBlockScope();
            scope.bind(id, value);
            dispatchElementBuilder(scope, loop.getElements());
        }
    }

    private void buildElementIntoScope(Scope parent, ComponentNode node) {
        var evaluator = parent.getEvaluator();
        var factory = getFactory(
                node.getType().getSourceLoc(),
                evaluator.evaluateToParameteriezedEntity(node.getType()));
        node.getSymbols().stream()
                .map(evaluator::evaluateToIdentifier)
                .forEach(id -> parent.bind(id, factory.create(id)));
    }

    private Component.Factory<?> getFactory(SourceLoc sourceLoc, ParameteriezedEntity type) {
        return switch (type.identifier().string()) {
            case "wire" -> {
                Types.requiresArgumentSize(1, type);
                yield Wire.Factory.of(getWidth(
                        Types.requiresParameterizedEntity(
                                sourceLoc, type.parameters().getFirst())));
            }
            case "clock" -> {
                Types.requiresArgumentSize(0, type);
                yield Clock.Factory.of();
            }
            case "reg" -> {
                Types.requiresArgumentSize(2, 3, type);
                // TODO: trigger_on, reset_on
                yield Register.Factory.of(getWidth(
                        Types.requiresParameterizedEntity(
                                sourceLoc, type.parameters().getFirst())));
            }
            default -> {
                log.error("{}: Unknown component type {}",
                        type.identifier().getSourceLoc(), type.identifier());
                throw new CompileCancellationException();
            }
        };
    }

    private int getWidth(ParameteriezedEntity type) {
        var sourceLoc = type.getIdentifier().getSourceLoc();
        if ("bit".equals(type.identifier().string())) {
            Types.requiresArgumentSize(1, type);
            return Types.requiresIntegerValue(sourceLoc, type.parameters().getFirst()).toInt();
        } else {
            log.error("{}: Expected bit type but got {}", sourceLoc, type);
            throw new CompileCancellationException();
        }
    }


    private void buildElementIntoScope(Scope parent, InstantiationNode instantiation) {
        throw new TodoException(); // TODO:
    }

}
