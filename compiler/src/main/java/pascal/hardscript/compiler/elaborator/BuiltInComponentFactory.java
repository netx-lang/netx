package pascal.hardscript.compiler.elaborator;

import pascal.hardscript.compiler.model.component.BuiltinAtom;
import pascal.hardscript.compiler.model.instance.Wire;
import pascal.hardscript.compiler.model.instance.type.BitType;
import pascal.hardscript.compiler.model.instance.type.InstanceType;
import pascal.hardscript.compiler.model.instance.type.PolyType;
import pascal.hardscript.compiler.model.scope.Attributes;
import pascal.hardscript.compiler.model.scope.ComponentScope;
import pascal.hardscript.compiler.model.scope.Scope;
import pascal.hardscript.util.lang.BuiltinComponent;
import pascal.hardscript.util.lang.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

import static pascal.hardscript.compiler.elaborator.Types.*;
import static pascal.hardscript.util.lang.BuiltinComponent.*;

public class BuiltInComponentFactory {
    static final Map<BuiltinComponent, BiFunction<Scope, BuiltinAtom, ComponentScope>> BUILTIN_FACTORY;

    static {
        BUILTIN_FACTORY = new HashMap<>();

        BUILTIN_FACTORY.put(REGISTER, BuiltInComponentFactory::createREGISTERComponent);
        BUILTIN_FACTORY.put(FROM, BuiltInComponentFactory::createFROMComponent);
        BUILTIN_FACTORY.put(IGNORE, BuiltInComponentFactory::createIGNOREComponent);
        BUILTIN_FACTORY.put(SPLIT, BuiltInComponentFactory::createSPLITComponent);
        BUILTIN_FACTORY.put(TO, BuiltInComponentFactory::createTOComponent);
        BUILTIN_FACTORY.put(EXT, BuiltInComponentFactory::createEXTComponent);

        BUILTIN_FACTORY.put(NOT, BuiltInComponentFactory::createNOTComponent);
        for (var op : Set.of(OR, AND, XOR, NOR, NAND, XNOR, CONCAT, ADD, SUB)) {
            BUILTIN_FACTORY.put(op, BuiltInComponentFactory::createMultiInputComponent);
        }
        for (var op : Set.of(EQ, NE, SLT, SLE, SGT, SGE, ULT, ULE, UGT, UGE)) {
            BUILTIN_FACTORY.put(op, BuiltInComponentFactory::createRelationalOpComponent);
        }
        for (var op : Set.of(SHL, ASHR, LSHR)) {
            BUILTIN_FACTORY.put(op, BuiltInComponentFactory::createShiftComponent);
        }
    }

    public static ComponentScope createBuiltInComponent(Scope parent, BuiltinAtom type) {
        var factory = BUILTIN_FACTORY.get(type.getBuiltin());
        return factory.apply(parent, type);
    }

    public static ComponentScope createFROMComponent(Scope parent, BuiltinAtom type) {
        var result = instantiate(parent, type);
        requiresMatchedSize(type.getSourceLoc(), 1, type.getParameters());
        var data = requiresInstance(type.getSourceLoc(), type.getParameters().getFirst());
        result.addOutput(data);
        return result;
    }

    public static ComponentScope createIGNOREComponent(Scope parent, BuiltinAtom type) {
        var result = instantiate(parent, type);
        result.addInput(newWire(result, "i"));
        return result;
    }

    @SuppressWarnings("JavaExistingMethodCanBeUsed")
    private static ComponentScope createNOTComponent(Scope parent, BuiltinAtom type) {
        var result = instantiate(parent, type);
        result.addInput(newWire(result, "i"));
        result.addOutput(newWire(result, "o"));
        return result;
    }

    private static ComponentScope createTOComponent(Scope parent, BuiltinAtom type) {
        var result = instantiate(parent, type);
        var entity = newWire(result, "t");
        result.addInput(entity);
        result.addOutput(entity);
        return result;
    }

    private static ComponentScope createREGISTERComponent(Scope parent, BuiltinAtom type) {
        requiresMatchedSize(type.getSourceLoc(), 3, type.getParameters());
        var result = instantiate(parent, type);

        var attributes = new Attributes();
        var clock = requiresInstance(type.getSourceLoc(), type.getParameters().get(0));
        var isPosTrigger = requiresBooleanValue(type.getSourceLoc(), type.getParameters().get(1));
        attributes.addAttribute(Identifier.of("clock"), clock);
        attributes.addAttribute(Identifier.of("isPosTrigger"), isPosTrigger);
        result.setAttributes(attributes);

        var data = requiresInstance(type.getSourceLoc(), type.getParameters().get(2));
        var input = newWire(result, "i", data.getType());
        result.addInput(input);
        result.addOutput(data);
        return result;
    }

    private static ComponentScope createSPLITComponent(Scope parent, BuiltinAtom type) {
        requiresMatchedSize(type.getSourceLoc(), 1, type.getParameters());
        int split = requiresIntegerValue(type.getSourceLoc(), type.getParameters().getFirst()).toInt();

        var Component = instantiate(parent, type);
        var entity = newWire(Component, "entity");
        Component.addInput(entity);
        for (int i = 0; i < split; ++i) {
            Component.addOutput(entity);
        }
        return Component;
    }

    private static ComponentScope createEXTComponent(Scope parent, BuiltinAtom type) {
        var result = instantiate(parent, type);
        if (type.getParameters().isEmpty()) {
            result.addInput(newWire(result, "i"));
            result.addOutput(newWire(result, "o"));
        } else {
            requiresMatchedSize(type.getSourceLoc(), 1, type.getParameters());
            int width = requiresIntegerValue(type.getSourceLoc(), type.getParameters().getFirst()).toInt();
            result.addInput(newWire(result, "i"));
            result.addOutput(newWire(result, "o", BitType.of(width)));
        }
        return result;
    }

    private static ComponentScope createRelationalOpComponent(Scope parent, BuiltinAtom type) {
        requiresMatchedSize(type.getSourceLoc(), 0, type.getParameters());
        var result = instantiate(parent, type);
        result.addInput(newWire(result, "i1"), newWire(result, "i2"));
        result.addOutput(newWire(result, "o", BitType.forBoolean()));
        return result;
    }

    private static ComponentScope createShiftComponent(Scope parent, BuiltinAtom type) {
        requiresMatchedSize(type.getSourceLoc(), 0, type.getParameters());
        var result = instantiate(parent, type);
        result.addInput(newWire(result, "i"), newWire(result, "off"));
        result.addOutput(newWire(result, "o"));
        return result;
    }

    private static ComponentScope createMultiInputComponent(Scope parent, BuiltinAtom type) {
        int ports = 2;
        if (!type.getParameters().isEmpty()) {
            requiresMatchedSize(type.getSourceLoc(), 1, type.getParameters());
            ports = requiresIntegerValue(type.getSourceLoc(), type.getParameters().getFirst()).toInt();
        }

        var result = instantiate(parent, type);
        for (int i = 1; i <= ports; i++) {
            result.addInput(newWire(result, "i" + i));
        }
        result.addOutput(newWire(result, "o"));
        return result;
    }

    private static ComponentScope instantiate(Scope parent, BuiltinAtom type) {
        return parent.instantiateComponentScope(
                Identifier.of(type.getBuiltin().name()).setSourceLoc(type.getSourceLoc())
        );
    }

    private static Wire newWire(ComponentScope result, String name) {
        return newWire(result, name, PolyType.getInstance());
    }

    private static Wire newWire(ComponentScope result, String name, InstanceType type) {
        var wire = Wire.of(Identifier.of("$" + name).setSourceLoc(result.getSourceLoc()), result, type);
        result.bind(wire.getIdentifier(), wire);
        return wire;
    }
}
