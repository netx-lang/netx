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
            "TO", BuiltInComponentFactory::createTOComponent,
            "OR", BuiltInComponentFactory::createORComponent,
            "REG", BuiltInComponentFactory::createREGComponent,
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
        var Component = parent.instantiateComponentScope(parent.getNameAllocator().allocateFor(type));
        Component.addInput(newWire(Component, "i", 1));
        Component.addOutput(newWire(Component, "o", 1));
        return Component;
    }

    private static ComponentScope createTOComponent(Scope parent, AtomType type) {
        var Component = parent.instantiateComponentScope(parent.getNameAllocator().allocateFor(type));
        var entity = newWire(Component, "t", 1);
        Component.addInput(entity);
        Component.addOutput(entity);
        return Component;
    }

    private static ComponentScope createREGComponent(Scope parent, AtomType type) {
        var sourceLoc = type.identifier().getSourceLoc();
        requiresMatchedSize(sourceLoc, 1, type.parameters());
        var component = requiresComponent(sourceLoc, type.parameters().getFirst());
        var Component = parent.instantiateComponentScope(parent.getNameAllocator().allocateFor(type));
        var input = newWire(Component, "input", component.getWidth());
        Component.addInput(input);
        Component.alias(input, component);
        Component.addOutput(component);
        return Component;
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

        var Component = parent.instantiateComponentScope(parent.getNameAllocator().allocateFor(type));
        for (int i = 1; i <= ports; i++) {
            Component.addInput(newWire(Component, "i" + i, 1));
        }
        Component.addOutput(newWire(Component, "o", 1));
        return Component;
    }

    private static Wire newWire(ComponentScope Component, String name, int size) {
        var wire = Wire.of(Identifier.of("$" + name), Component, size);
        Component.bind(wire.getIdentifier(), wire);
        return wire;
    }
}
