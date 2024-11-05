package pascal.hardscript.compiler.elaborator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pascal.hardscript.compiler.ast.Identifier;
import pascal.hardscript.compiler.exception.CompileCancellationException;
import pascal.hardscript.compiler.model.component.AtomType;
import pascal.hardscript.compiler.model.data.AutoInferenceType;
import pascal.hardscript.compiler.model.data.DataType;
import pascal.hardscript.compiler.model.data.Wire;
import pascal.hardscript.compiler.model.scope.ComponentScope;
import pascal.hardscript.compiler.model.scope.Scope;
import pascal.hardscript.util.lang.BuiltinComponent;

import java.util.Map;
import java.util.function.BiFunction;

import static pascal.hardscript.compiler.elaborator.Types.*;
import static pascal.hardscript.util.lang.BuiltinComponent.*;

public class BuiltInComponentFactory {
    private static final Logger log = LoggerFactory.getLogger(BuiltInComponentFactory.class);
    static final Map<BuiltinComponent, BiFunction<Scope, AtomType, ComponentScope>> BUILTIN_FACTORY = Map.ofEntries(
            Map.entry(REGISTER, BuiltInComponentFactory::createREGISTERComponent),
            Map.entry(SOURCE, BuiltInComponentFactory::createSOURCEComponent),
            Map.entry(IGNORE, BuiltInComponentFactory::createIGNOREComponent),
            Map.entry(SPLIT, BuiltInComponentFactory::createSPLITComponent),
            Map.entry(TO, BuiltInComponentFactory::createTOComponent),
            Map.entry(OR, BuiltInComponentFactory::createORComponent),
            Map.entry(NOT, BuiltInComponentFactory::createNOTComponent),
            Map.entry(AND, BuiltInComponentFactory::createANDComponent),
            Map.entry(XOR, BuiltInComponentFactory::createXORComponent),
            Map.entry(NOR, BuiltInComponentFactory::createNORComponent),
            Map.entry(NAND, BuiltInComponentFactory::createNANDComponent)
    );

    public static ComponentScope createBuiltInComponent(Scope parent, AtomType type) {
        var identifier = type.identifier();
        var factory = BUILTIN_FACTORY.get(BuiltinComponent.of(identifier.string()));
        if (factory == null) {
            log.error("{}: Unknown built-in Component: {}", identifier.getSourceLoc(), identifier);
            throw new CompileCancellationException();
        }
        return factory.apply(parent, type);
    }

    public static ComponentScope createSOURCEComponent(Scope parent, AtomType type) {
        var result = parent.instantiateComponentScope(parent.getNameAllocator().allocateFor(type));
        var sourceLoc = type.identifier().getSourceLoc();
        requiresMatchedSize(sourceLoc, 1, type.parameters());

        var data = requiresData(sourceLoc, type.parameters().getFirst());
        result.addOutput(data);
        return result;
    }

    public static ComponentScope createIGNOREComponent(Scope parent, AtomType type) {
        var result = parent.instantiateComponentScope(parent.getNameAllocator().allocateFor(type));
        result.addInput(newWire(result, "i"));
        return result;
    }

    @SuppressWarnings("JavaExistingMethodCanBeUsed")
    private static ComponentScope createNOTComponent(Scope parent, AtomType type) {
        var result = parent.instantiateComponentScope(parent.getNameAllocator().allocateFor(type));
        result.addInput(newWire(result, "i"));
        result.addOutput(newWire(result, "o"));
        return result;
    }

    private static ComponentScope createTOComponent(Scope parent, AtomType type) {
        var result = parent.instantiateComponentScope(parent.getNameAllocator().allocateFor(type));
        var entity = newWire(result, "t");
        result.addInput(entity);
        result.addOutput(entity);
        return result;
    }

    private static ComponentScope createREGISTERComponent(Scope parent, AtomType type) {
        var sourceLoc = type.identifier().getSourceLoc();
        requiresMatchedSize(sourceLoc, 3, type.parameters());
        var result = parent.instantiateComponentScope(parent.getNameAllocator().allocateFor(type));

        var clock = requiresData(sourceLoc, type.parameters().get(0));
        var isPosTrigger = requiresBooleanValue(sourceLoc, type.parameters().get(1));
        result.addAttribute("clock", clock);
        result.addAttribute("isPosTrigger", isPosTrigger);

        var data = requiresData(sourceLoc, type.parameters().get(2));
        var input = newWire(result, "i", data.getType());
        result.addInput(input);
        result.addOutput(data);
        return result;
    }

    private static ComponentScope createSPLITComponent(Scope parent, AtomType type) {
        var sourceLoc = type.identifier().getSourceLoc();
        requiresMatchedSize(sourceLoc, 1, type.parameters());
        int split = requiresIntegerValue(sourceLoc, type.parameters().getFirst()).toInt();

        var Component = parent.instantiateComponentScope(parent.getNameAllocator().allocateFor(type));
        var entity = newWire(Component, "entity");
        Component.addInput(entity);
        for (int i = 0; i < split; ++i) {
            Component.addOutput(entity);
        }
        return Component;
    }

    private static ComponentScope createANDComponent(Scope parent, AtomType type) {
        return makeDualInputGateComponent(parent, type);
    }

    private static ComponentScope createORComponent(Scope parent, AtomType type) {
        return makeDualInputGateComponent(parent, type);
    }

    private static ComponentScope createXORComponent(Scope parent, AtomType type) {
        return makeDualInputGateComponent(parent, type);
    }

    private static ComponentScope createNANDComponent(Scope parent, AtomType type) {
        return makeDualInputGateComponent(parent, type);
    }

    private static ComponentScope createNORComponent(Scope parent, AtomType type) {
        return makeDualInputGateComponent(parent, type);
    }

    private static ComponentScope makeDualInputGateComponent(Scope parent, AtomType type) {
        int ports;
        if (type.parameters().isEmpty()) {
            ports = 2;
        } else {
            var sourceLoc = type.identifier().getSourceLoc();
            requiresMatchedSize(sourceLoc, 1, type.parameters());
            ports = requiresIntegerValue(sourceLoc, type.parameters().getFirst()).toInt();
        }

        var result = parent.instantiateComponentScope(parent.getNameAllocator().allocateFor(type));
        for (int i = 1; i <= ports; i++) {
            result.addInput(newWire(result, "i" + i));
        }
        result.addOutput(newWire(result, "o"));
        return result;
    }

    private static Wire newWire(ComponentScope result, String name) {
        return newWire(result, name, AutoInferenceType.getInstance());
    }

    private static Wire newWire(ComponentScope result, String name, DataType type) {
        var wire = Wire.of(Identifier.of("$" + name).setSourceLoc(result.getSourceLoc()), result, type);
        result.bind(wire.getIdentifier(), wire);
        return wire;
    }
}
