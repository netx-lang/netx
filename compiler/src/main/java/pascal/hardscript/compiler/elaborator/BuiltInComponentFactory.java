package pascal.hardscript.compiler.elaborator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pascal.hardscript.compiler.ast.Identifier;
import pascal.hardscript.compiler.exception.CompileCancellationException;
import pascal.hardscript.compiler.model.component.AtomType;
import pascal.hardscript.compiler.model.data.AutoInferenceType;
import pascal.hardscript.compiler.model.data.BitType;
import pascal.hardscript.compiler.model.data.DataType;
import pascal.hardscript.compiler.model.data.Wire;
import pascal.hardscript.compiler.model.scope.ComponentScope;
import pascal.hardscript.compiler.model.scope.Scope;
import pascal.hardscript.util.lang.BuiltinComponent;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

import static pascal.hardscript.compiler.elaborator.Types.*;
import static pascal.hardscript.util.lang.BuiltinComponent.*;

public class BuiltInComponentFactory {
    private static final Logger log = LoggerFactory.getLogger(BuiltInComponentFactory.class);
    static final Map<BuiltinComponent, BiFunction<Scope, AtomType, ComponentScope>> BUILTIN_FACTORY;

    static {
        BUILTIN_FACTORY = new HashMap<>();

        BUILTIN_FACTORY.put(REGISTER, BuiltInComponentFactory::createREGISTERComponent);
        BUILTIN_FACTORY.put(SOURCE, BuiltInComponentFactory::createSOURCEComponent);
        BUILTIN_FACTORY.put(IGNORE, BuiltInComponentFactory::createIGNOREComponent);
        BUILTIN_FACTORY.put(SPLIT, BuiltInComponentFactory::createSPLITComponent);
        BUILTIN_FACTORY.put(TO, BuiltInComponentFactory::createTOComponent);
        BUILTIN_FACTORY.put(EXT, BuiltInComponentFactory::createEXTComponent);

        BUILTIN_FACTORY.put(NOT, BuiltInComponentFactory::createNOTComponent);
        for (var op : Set.of(OR, AND, XOR, NOR, NAND, CONCAT, ADD, SUB)) {
            BUILTIN_FACTORY.put(op, BuiltInComponentFactory::createMultiInputComponent);
        }
        for (var op : Set.of(EQ, NE, SLT, SLE, SGT, SGE, ULT, ULE, UGT, UGE)) {
            BUILTIN_FACTORY.put(op, BuiltInComponentFactory::createRelationalOpComponent);
        }
        for (var op : Set.of(SHL, ASHR, LSHR)) {
            BUILTIN_FACTORY.put(op, BuiltInComponentFactory::createShiftComponent);
        }
    }

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
        var result = instantiate(parent, type);
        var sourceLoc = type.identifier().getSourceLoc();
        requiresMatchedSize(sourceLoc, 1, type.parameters());

        var data = requiresData(sourceLoc, type.parameters().getFirst());
        result.addOutput(data);
        return result;
    }

    public static ComponentScope createIGNOREComponent(Scope parent, AtomType type) {
        var result = instantiate(parent, type);
        result.addInput(newWire(result, "i"));
        return result;
    }

    @SuppressWarnings("JavaExistingMethodCanBeUsed")
    private static ComponentScope createNOTComponent(Scope parent, AtomType type) {
        var result = instantiate(parent, type);
        result.addInput(newWire(result, "i"));
        result.addOutput(newWire(result, "o"));
        return result;
    }

    private static ComponentScope createTOComponent(Scope parent, AtomType type) {
        var result = instantiate(parent, type);
        var entity = newWire(result, "t");
        result.addInput(entity);
        result.addOutput(entity);
        return result;
    }

    private static ComponentScope createREGISTERComponent(Scope parent, AtomType type) {
        var sourceLoc = type.identifier().getSourceLoc();
        requiresMatchedSize(sourceLoc, 3, type.parameters());
        var result = instantiate(parent, type);

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

        var Component = instantiate(parent, type);
        var entity = newWire(Component, "entity");
        Component.addInput(entity);
        for (int i = 0; i < split; ++i) {
            Component.addOutput(entity);
        }
        return Component;
    }

    private static ComponentScope createEXTComponent(Scope parent, AtomType type) {
        var sourceLoc = type.identifier().getSourceLoc();
        var result = instantiate(parent, type);
        if (type.parameters().isEmpty()) {
            result.addInput(newWire(result, "i"));
            result.addOutput(newWire(result, "o"));
        } else {
            requiresMatchedSize(sourceLoc, 1, type.parameters());
            int width = requiresIntegerValue(sourceLoc, type.parameters().getFirst()).toInt();
            result.addInput(newWire(result, "i"));
            result.addOutput(newWire(result, "o", BitType.of(width)));
        }
        return result;
    }

    private static ComponentScope createRelationalOpComponent(Scope parent, AtomType type) {
        var sourceLoc = type.identifier().getSourceLoc();
        requiresMatchedSize(sourceLoc, 0, type.parameters());
        var result = instantiate(parent, type);
        result.addInput(newWire(result, "i1"), newWire(result, "i2"));
        result.addOutput(newWire(result, "o", BitType.forBoolean()));
        return result;
    }

    private static ComponentScope createShiftComponent(Scope parent, AtomType type) {
        var sourLoc = type.identifier().getSourceLoc();
        requiresMatchedSize(sourLoc, 0, type.parameters());
        var result = instantiate(parent, type);
        result.addInput(newWire(result, "i"), newWire(result, "off"));
        result.addOutput(newWire(result, "o"));
        return result;
    }

    private static ComponentScope createMultiInputComponent(Scope parent, AtomType type) {
        int ports = 2;
        if (!type.parameters().isEmpty()) {
            var sourceLoc = type.identifier().getSourceLoc();
            requiresMatchedSize(sourceLoc, 1, type.parameters());
            ports = requiresIntegerValue(sourceLoc, type.parameters().getFirst()).toInt();
        }

        var result = instantiate(parent, type);
        for (int i = 1; i <= ports; i++) {
            result.addInput(newWire(result, "i" + i));
        }
        result.addOutput(newWire(result, "o"));
        return result;
    }

    private static ComponentScope instantiate(Scope parent, AtomType type) {
        return parent.instantiateComponentScope(parent.getNameAllocator().allocateFor(type));
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
