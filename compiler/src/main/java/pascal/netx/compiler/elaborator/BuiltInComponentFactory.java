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
import pascal.netx.util.lang.BuiltinComponent;
import pascal.netx.util.lang.Identifier;

import static pascal.netx.compiler.model.Entities.requiresBooleanValue;
import static pascal.netx.compiler.model.Entities.requiresIntegerValue;
import static pascal.netx.util.lang.BuiltinComponent.REGISTER_ATTRIBUTE;

public class BuiltInComponentFactory {
    private final NXCompiler compiler;

    public BuiltInComponentFactory(NXCompiler compiler) {
        this.compiler = compiler;
    }

    public static BuiltInComponentFactory create(NXCompiler compiler) {
        return new BuiltInComponentFactory(compiler);
    }

    public ComponentScope createBuiltInComponent(Scope parent, BuiltinRef ref, int argNumHint) {
        if (BuiltinComponent.MULTI_INPUT.contains(ref.getBuiltin())) {
            return createMultiInputComponent(parent, ref, argNumHint);
        } else {
            return createBuiltInComponent(parent, ref);
        }
    }

    public ComponentScope createBuiltInComponent(Scope parent, BuiltinRef ref) {
        return switch (ref.getBuiltin()) {
            case REGISTER -> createRegisterComponent(parent, ref);
            case SPLIT -> createSplitComponent(parent, ref);
            case TO -> createIdentityComponent(parent, ref);
            case CAST -> createCastComponent(parent, ref);
            case NOT -> createNotComponent(parent, ref);
            case OR, AND, XOR, NOR, NAND, XNOR, CONCAT, ADD, SUB, MUL, DIV, MOD ->
                    createMultiInputComponent(parent, ref, 2);
            case EQ, NE, SLT, SLE, SGT, SGE, ULT, ULE, UGT, UGE -> createRelationalOpComponent(parent, ref);
            case SHL, ASHR, LSHR -> createShiftComponent(parent, ref);
        };
    }

    private ComponentScope createNotComponent(Scope parent, BuiltinRef ref) {
        var result = instantiate(parent, ref);
        result.addInput(newWire(result, "i"));
        result.addOutput(newWire(result, "o"));
        return result;
    }

    private ComponentScope createIdentityComponent(Scope parent, BuiltinRef ref) {
        var result = instantiate(parent, ref);
        var entity = newWire(result, "t");
        result.addInput(entity);
        result.addOutput(entity);
        return result;
    }

    private ComponentScope createRegisterComponent(Scope parent, BuiltinRef ref) {
        Utils.requiresMatchedSize(ref.getSourceLoc(), 1, ref.getArguments());
        var result = instantiate(parent, ref);
        var attributes = new Attributes();
        var isPosTrigger = requiresBooleanValue(ref.getSourceLoc(), ref.getArguments().getFirst());
        attributes.addAttribute(Identifier.of(REGISTER_ATTRIBUTE), isPosTrigger);
        result.setAttributes(attributes);
        result.addInput(newWire(result, "i"));
        result.addInput(newClock(result));
        result.addOutput(newWire(result, "o"));
        return result;
    }

    private ComponentScope createSplitComponent(Scope parent, BuiltinRef ref) {
        Utils.requiresMatchedSize(ref.getSourceLoc(), 1, ref.getArguments());
        int split = requiresIntegerValue(ref.getSourceLoc(), ref.getArguments().getFirst()).toInt();

        var Component = instantiate(parent, ref);
        var entity = newWire(Component, "entity");
        Component.addInput(entity);
        for (int i = 0; i < split; ++i) {
            Component.addOutput(entity);
        }
        return Component;
    }

    private ComponentScope createCastComponent(Scope parent, BuiltinRef ref) {
        var result = instantiate(parent, ref);
        if (ref.getArguments().isEmpty()) {
            result.addInput(newWire(result, "i"));
            result.addOutput(newWire(result, "o"));
        } else {
            Utils.requiresMatchedSize(ref.getSourceLoc(), 1, ref.getArguments());
            int width = requiresIntegerValue(ref.getSourceLoc(), ref.getArguments().getFirst()).toInt();
            result.addInput(newWire(result, "i"));
            result.addOutput(newWire(result, "o", BitType.of(width)));
        }
        return result;
    }

    private ComponentScope createRelationalOpComponent(Scope parent, BuiltinRef ref) {
        Utils.requiresMatchedSize(ref.getSourceLoc(), 0, ref.getArguments());
        var result = instantiate(parent, ref);
        result.addInput(newWire(result, "i1"), newWire(result, "i2"));
        result.addOutput(newWire(result, "o", BitType.forBoolean()));
        return result;
    }

    private ComponentScope createShiftComponent(Scope parent, BuiltinRef ref) {
        Utils.requiresMatchedSize(ref.getSourceLoc(), 0, ref.getArguments());
        var result = instantiate(parent, ref);
        result.addInput(newWire(result, "i"), newWire(result, "off"));
        result.addOutput(newWire(result, "o"));
        return result;
    }

    private ComponentScope createMultiInputComponent(Scope parent, BuiltinRef ref, int argNumHint) {
        int ports;
        if (!ref.getArguments().isEmpty()) {
            Utils.requiresMatchedSize(ref.getSourceLoc(), 1, ref.getArguments());
            ports = requiresIntegerValue(ref.getSourceLoc(), ref.getArguments().getFirst()).toInt();
        } else {
            ports = argNumHint;
        }

        var result = instantiate(parent, ref);
        for (int i = 1; i <= ports; i++) {
            result.addInput(newWire(result, "i" + i));
        }
        result.addOutput(newWire(result, "o"));
        return result;
    }

    private ComponentScope instantiate(Scope parent, BuiltinRef ref) {
        return ComponentScope.newComponent(parent, ref.getBuiltin().asID().setSourceLoc(ref.getSourceLoc()));
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
