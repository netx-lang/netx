package pascal.netx.compiler.elaborator;

import pascal.netx.compiler.io.NXCompiler;
import pascal.netx.compiler.model.Utils;
import pascal.netx.compiler.model.expr.BuiltinRef;
import pascal.netx.compiler.model.scope.Attributes;
import pascal.netx.compiler.model.scope.ComponentScope;
import pascal.netx.compiler.model.scope.Scope;
import pascal.netx.compiler.model.variable.Clock;
import pascal.netx.compiler.model.variable.Wire;
import pascal.netx.compiler.model.variable.type.BitType;
import pascal.netx.compiler.model.variable.type.PolyType;
import pascal.netx.compiler.model.variable.type.Type;
import pascal.netx.util.lang.Identifier;

import static pascal.netx.compiler.model.Entities.requiresBooleanValue;
import static pascal.netx.compiler.model.Entities.requiresIntegerValue;

public class BuiltInComponentFactory {
    private final NXCompiler compiler;

    public BuiltInComponentFactory(NXCompiler compiler) {
        this.compiler = compiler;
    }

    public ComponentScope createBuiltInComponent(Scope parent, BuiltinRef type) {
        return switch (type.getBuiltin()) {
            case REGISTER -> createREGISTERComponent(parent, type);
            case IGNORE -> createIGNOREComponent(parent, type);
            case SPLIT -> createSPLITComponent(parent, type);
            case TO -> createTOComponent(parent, type);
            case CAST -> createCASTComponent(parent, type);
            case NOT -> createNOTComponent(parent, type);
            case OR, AND, XOR, NOR, NAND, XNOR, CONCAT, ADD, SUB, MUL, DIV, MOD ->
                    createMultiInputComponent(parent, type);
            case EQ, NE, SLT, SLE, SGT, SGE, ULT, ULE, UGT, UGE -> createRelationalOpComponent(parent, type);
            case SHL, ASHR, LSHR -> createShiftComponent(parent, type);
        };
    }

    public ComponentScope createIGNOREComponent(Scope parent, BuiltinRef type) {
        var result = instantiate(parent, type);
        result.addInput(newWire(result, "i"));
        return result;
    }

    private ComponentScope createNOTComponent(Scope parent, BuiltinRef type) {
        var result = instantiate(parent, type);
        result.addInput(newWire(result, "i"));
        result.addOutput(newWire(result, "o"));
        return result;
    }

    private ComponentScope createTOComponent(Scope parent, BuiltinRef type) {
        var result = instantiate(parent, type);
        var entity = newWire(result, "t");
        result.addInput(entity);
        result.addOutput(entity);
        return result;
    }

    private ComponentScope createREGISTERComponent(Scope parent, BuiltinRef type) {
        Utils.requiresMatchedSize(type.getSourceLoc(), 1, type.getParameters());
        var result = instantiate(parent, type);
        var attributes = new Attributes();
        var isPosTrigger = requiresBooleanValue(type.getSourceLoc(), type.getParameters().getFirst());
        attributes.addAttribute(Identifier.of("isPosTrigger"), isPosTrigger);
        result.setAttributes(attributes);
        result.addInput(newWire(result, "i"));
        result.addInput(newClock(result));
        result.addOutput(newWire(result, "o"));
        return result;
    }

    private ComponentScope createSPLITComponent(Scope parent, BuiltinRef type) {
        Utils.requiresMatchedSize(type.getSourceLoc(), 1, type.getParameters());
        int split = requiresIntegerValue(type.getSourceLoc(), type.getParameters().getFirst()).toInt();

        var Component = instantiate(parent, type);
        var entity = newWire(Component, "entity");
        Component.addInput(entity);
        for (int i = 0; i < split; ++i) {
            Component.addOutput(entity);
        }
        return Component;
    }

    private ComponentScope createCASTComponent(Scope parent, BuiltinRef type) {
        var result = instantiate(parent, type);
        if (type.getParameters().isEmpty()) {
            result.addInput(newWire(result, "i"));
            result.addOutput(newWire(result, "o"));
        } else {
            Utils.requiresMatchedSize(type.getSourceLoc(), 1, type.getParameters());
            int width = requiresIntegerValue(type.getSourceLoc(), type.getParameters().getFirst()).toInt();
            result.addInput(newWire(result, "i"));
            result.addOutput(newWire(result, "o", BitType.of(width)));
        }
        return result;
    }

    private ComponentScope createRelationalOpComponent(Scope parent, BuiltinRef type) {
        Utils.requiresMatchedSize(type.getSourceLoc(), 0, type.getParameters());
        var result = instantiate(parent, type);
        result.addInput(newWire(result, "i1"), newWire(result, "i2"));
        result.addOutput(newWire(result, "o", BitType.forBoolean()));
        return result;
    }

    private ComponentScope createShiftComponent(Scope parent, BuiltinRef type) {
        Utils.requiresMatchedSize(type.getSourceLoc(), 0, type.getParameters());
        var result = instantiate(parent, type);
        result.addInput(newWire(result, "i"), newWire(result, "off"));
        result.addOutput(newWire(result, "o"));
        return result;
    }

    private ComponentScope createMultiInputComponent(Scope parent, BuiltinRef type) {
        int ports = 2;
        if (!type.getParameters().isEmpty()) {
            Utils.requiresMatchedSize(type.getSourceLoc(), 1, type.getParameters());
            ports = requiresIntegerValue(type.getSourceLoc(), type.getParameters().getFirst()).toInt();
        }

        var result = instantiate(parent, type);
        for (int i = 1; i <= ports; i++) {
            result.addInput(newWire(result, "i" + i));
        }
        result.addOutput(newWire(result, "o"));
        return result;
    }

    private ComponentScope instantiate(Scope parent, BuiltinRef type) {
        return ComponentScope.newComponent(parent,
                Identifier.of(type.getBuiltin().name()).setSourceLoc(type.getSourceLoc()));
    }

    private Wire newWire(ComponentScope result, String name) {
        return newWire(result, name, PolyType.getInstance());
    }

    private Clock newClock(ComponentScope result) {
        var id = Identifier.of("$clk").setSourceLoc(result.getSourceLoc());
        var clock = new Clock.Factory(compiler).create(result, id);
        result.bind(clock.getIdentifier(), clock);
        return clock;
    }

    private Wire newWire(ComponentScope result, String name, Type type) {
        var id = Identifier.of("$" + name).setSourceLoc(result.getSourceLoc());
        var wire = new Wire.Factory(type, compiler).create(result, id);
        result.bind(wire.getIdentifier(), wire);
        return wire;
    }
}
