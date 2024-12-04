package pascal.hardscript.compiler.elaborator;

import pascal.hardscript.compiler.model.component.BuiltinAtom;
import pascal.hardscript.compiler.model.instance.Wire;
import pascal.hardscript.compiler.model.instance.type.BitType;
import pascal.hardscript.compiler.model.instance.type.InstanceType;
import pascal.hardscript.compiler.model.instance.type.PolyType;
import pascal.hardscript.compiler.model.scope.Attributes;
import pascal.hardscript.compiler.model.scope.ComponentScope;
import pascal.hardscript.compiler.model.scope.GlobalScope;
import pascal.hardscript.compiler.model.scope.Scope;
import pascal.hardscript.util.lang.Identifier;

import static pascal.hardscript.compiler.elaborator.Types.*;

public class BuiltInComponentFactory {
    private final GlobalScope global;

    public BuiltInComponentFactory(GlobalScope global) {
        this.global = global;
    }

    public ComponentScope createBuiltInComponent(Scope parent, BuiltinAtom type) {
        return switch (type.getBuiltin()) {
            case REGISTER -> createREGISTERComponent(parent, type);
            case IGNORE -> createIGNOREComponent(parent, type);
            case SPLIT -> createSPLITComponent(parent, type);
            case TO -> createTOComponent(parent, type);
            case CAST -> createCASTComponent(parent, type);
            case NOT -> createNOTComponent(parent, type);
            case OR, AND, XOR, NOR, NAND, XNOR, CONCAT, ADD, SUB, MUL, DIV, MOD -> createMultiInputComponent(parent, type);
            case EQ, NE, SLT, SLE, SGT, SGE, ULT, ULE, UGT, UGE -> createRelationalOpComponent(parent, type);
            case SHL, ASHR, LSHR -> createShiftComponent(parent, type);
        };
    }

    public ComponentScope createIGNOREComponent(Scope parent, BuiltinAtom type) {
        var result = instantiate(parent, type);
        result.addInput(newWire(result, "i"));
        return result;
    }

    private ComponentScope createNOTComponent(Scope parent, BuiltinAtom type) {
        var result = instantiate(parent, type);
        result.addInput(newWire(result, "i"));
        result.addOutput(newWire(result, "o"));
        return result;
    }

    private ComponentScope createTOComponent(Scope parent, BuiltinAtom type) {
        var result = instantiate(parent, type);
        var entity = newWire(result, "t");
        result.addInput(entity);
        result.addOutput(entity);
        return result;
    }

    private ComponentScope createREGISTERComponent(Scope parent, BuiltinAtom type) {
        requiresMatchedSize(type.getSourceLoc(), 2, type.getParameters());
        var result = instantiate(parent, type);
        var attributes = new Attributes();
        var isPosTrigger = requiresBooleanValue(type.getSourceLoc(), type.getParameters().get(1));
        attributes.addAttribute(Identifier.of("isPosTrigger"), isPosTrigger);
        result.setAttributes(attributes);
        result.addInput(newWire(result, "i"));
        result.addOutput(newWire(result, "o"));
        return result;
    }

    private ComponentScope createSPLITComponent(Scope parent, BuiltinAtom type) {
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

    private ComponentScope createCASTComponent(Scope parent, BuiltinAtom type) {
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

    private ComponentScope createRelationalOpComponent(Scope parent, BuiltinAtom type) {
        requiresMatchedSize(type.getSourceLoc(), 0, type.getParameters());
        var result = instantiate(parent, type);
        result.addInput(newWire(result, "i1"), newWire(result, "i2"));
        result.addOutput(newWire(result, "o", BitType.forBoolean()));
        return result;
    }

    private ComponentScope createShiftComponent(Scope parent, BuiltinAtom type) {
        requiresMatchedSize(type.getSourceLoc(), 0, type.getParameters());
        var result = instantiate(parent, type);
        result.addInput(newWire(result, "i"), newWire(result, "off"));
        result.addOutput(newWire(result, "o"));
        return result;
    }

    private ComponentScope createMultiInputComponent(Scope parent, BuiltinAtom type) {
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

    private ComponentScope instantiate(Scope parent, BuiltinAtom type) {
        return parent.instantiateComponentScope(
                Identifier.of(type.getBuiltin().name()).setSourceLoc(type.getSourceLoc()));
    }

    private Wire newWire(ComponentScope result, String name) {
        return newWire(result, name, PolyType.getInstance());
    }

    private Wire newWire(ComponentScope result, String name, InstanceType type) {
        var id = Identifier.of("$" + name).setSourceLoc(result.getSourceLoc());
        var wire = new Wire.Factory(type, global).create(result, id);
        result.bind(wire.getIdentifier(), wire);
        return wire;
    }
}
