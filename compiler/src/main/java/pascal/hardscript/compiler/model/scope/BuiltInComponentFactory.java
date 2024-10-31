package pascal.hardscript.compiler.model.scope;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pascal.hardscript.compiler.ast.Identifier;
import pascal.hardscript.compiler.exception.CompileCancellationException;
import pascal.hardscript.compiler.model.component.AtomType;
import pascal.hardscript.compiler.model.data.Wire;

import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

import static pascal.hardscript.compiler.elaborator.Types.*;

public class BuiltInComponentFactory {
    private static final Logger log = LoggerFactory.getLogger(BuiltInComponentFactory.class);
    private static final Map<String, BiFunction<Scope, AtomType, ComponentScope>> BUILTIN_FACTORY = Map.of(
            "REGISTER", BuiltInComponentFactory::createREGISTERComponent,
            "TO", BuiltInComponentFactory::createTOComponent,
            "OR", BuiltInComponentFactory::createORComponent,
            "SPLIT", BuiltInComponentFactory::createSPLITComponent,
            "NOT", BuiltInComponentFactory::createNOTComponent,
            "AND", BuiltInComponentFactory::createANDComponent,
            "XOR", BuiltInComponentFactory::createXORComponent,
            "NOR", BuiltInComponentFactory::createNORComponent,
            "NAND", BuiltInComponentFactory::createNANDComponent
    );
    public static final Set<String> BUILTIN = BUILTIN_FACTORY.keySet();

    public static ComponentScope createBuiltInComponent(Scope parent, AtomType type) {
        var identifier = type.identifier();
        var factory = BUILTIN_FACTORY.get(identifier.string());
        if (factory == null) {
            log.error("{}: Unknown built-in Component: {}", identifier.getSourceLoc(), identifier);
            throw new CompileCancellationException();
        }
        return factory.apply(parent, type);
    }

    private static ComponentScope createNOTComponent(Scope parent, AtomType type) {
        var result = parent.instantiateComponentScope(parent.getNameAllocator().allocateFor(type));
        result.addInput(newWire(result, "i", 1));
        result.addOutput(newWire(result, "o", 1));
        return result;
    }

    private static ComponentScope createTOComponent(Scope parent, AtomType type) {
        var result = parent.instantiateComponentScope(parent.getNameAllocator().allocateFor(type));
        var entity = newWire(result, "t", 1);
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
        var input = newWire(result, "i", data.getWidth());
        result.addInput(input);
        result.addOutput(data);
        return result;
    }

    private static ComponentScope createSPLITComponent(Scope parent, AtomType type) {
        var sourceLoc = type.identifier().getSourceLoc();
        requiresMatchedSize(sourceLoc, 1, type.parameters());
        int split = requiresIntegerValue(sourceLoc, type.parameters().getFirst()).toInt();

        var Component = parent.instantiateComponentScope(parent.getNameAllocator().allocateFor(type));
        var entity = newWire(Component, "entity", 1);
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
            result.addInput(newWire(result, "i" + i, 1));
        }
        result.addOutput(newWire(result, "o", 1));
        return result;
    }

    private static Wire newWire(ComponentScope result, String name, int size) {
        var wire = Wire.of(Identifier.of("$" + name), result, size);
        result.bind(wire.getIdentifier(), wire);
        return wire;
    }
}
