package pascal.netx.compiler.elaborator;

import pascal.netx.compiler.io.NXCompiler;
import pascal.netx.compiler.model.Entities;
import pascal.netx.compiler.model.Entity;
import pascal.netx.compiler.model.Utils;
import pascal.netx.compiler.model.expr.BuiltinRef;
import pascal.netx.compiler.model.scope.Attributes;
import pascal.netx.compiler.model.scope.ComponentScope;
import pascal.netx.compiler.model.scope.Scope;
import pascal.netx.compiler.model.variable.Clock;
import pascal.netx.compiler.model.variable.Variable;
import pascal.netx.compiler.model.variable.Wire;
import pascal.netx.compiler.model.variable.selector.Selector;
import pascal.netx.compiler.model.variable.type.BitType;
import pascal.netx.compiler.model.variable.type.PolyType;
import pascal.netx.compiler.model.variable.type.Type;
import pascal.netx.util.lang.BuiltinComponent;
import pascal.netx.util.lang.Identifier;
import pascal.netx.util.lang.SourceLoc;

import java.util.function.BiFunction;

import static pascal.netx.compiler.model.Entities.requiresIntegerValue;
import static pascal.netx.util.lang.BuiltinComponent.REG_CLK_EDGE_ATTR;
import static pascal.netx.util.lang.BuiltinComponent.TO;

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

    private static void moveAttribute(Attributes attr, BuiltinRef ref, String name, int index,
                                      BiFunction<SourceLoc, Entity, ? extends  Entity> checker) {
        attr.addAttribute(Identifier.of(name), checker.apply(ref.getSourceLoc(), ref.getArgList().get(name, index)));
    }

    private ComponentScope createNotComponent(Scope parent, BuiltinRef ref) {
        var result = instantiate(parent, ref);
        result.addInput(newWire(result, "i"));
        result.addOutput(newWire(result, "o"));
        return result;
    }

    private ComponentScope createIdentityComponent(Scope parent, BuiltinRef ref) {
        var result = instantiate(parent, ref);
        var entity = newWire(result, "TO_DUMMY");
        result.addInput(entity);
        result.addOutput(entity);
        return result;
    }

    public ComponentScope createBuiltInComponent(Scope parent, BuiltinRef ref) {
        return switch (ref.getBuiltin()) {
            case REGISTER -> createRegisterComponent(parent, ref);
            case ASYNC_REGISTER -> createAsyncRegisterComponent(parent, ref);
            case DUP -> createDupComponent(parent, ref);
            case SPLIT -> createSplitComponent(parent, ref);
            case TO -> createIdentityComponent(parent, ref);
            case UCAST, CAST -> createCastComponent(parent, ref);
            case NOT -> createNotComponent(parent, ref);
            case OR, AND, XOR, NOR, NAND, XNOR, CONCAT, ADD, MUL ->
                    createMultiInputComponent(parent, ref, 2);
            case SUB, DIV, MOD ->  createDualInputComponent(parent, ref);
            case EQ, NE, SLT, SLE, SGT, SGE, ULT, ULE, UGT, UGE -> createRelationalOpComponent(parent, ref);
            case SHL, ASHR, LSHR -> createShiftComponent(parent, ref);
        };
    }

    private ComponentScope createRegisterComponent(Scope parent, BuiltinRef ref) {
        Utils.requiresMatchedSize(ref.getSourceLoc(), 1, ref.getArgList());
        var result = instantiate(parent, ref);
        var attributes = new Attributes();
        moveAttribute(attributes, ref, REG_CLK_EDGE_ATTR, 0, Entities::requiresBooleanValue);
        result.setAttributes(attributes);
        result.addInput(newClock(result));
        result.addInput(newWire(result, "i"));
        result.addOutput(newWire(result, "o"));
        return result;
    }

    private ComponentScope createAsyncRegisterComponent(Scope parent, BuiltinRef ref) {
        var loc = ref.getSourceLoc();

        Utils.requiresMatchedSize(loc, 3, ref.getArgList());
        var result = instantiate(parent, ref);
        var attributes = new Attributes();
        moveAttribute(attributes, ref, REG_CLK_EDGE_ATTR, 0, Entities::requiresBooleanValue);
        moveAttribute(attributes, ref, BuiltinComponent.REG_RST_EDGE_ATTR, 1, Entities::requiresBooleanValue);
        moveAttribute(attributes, ref, BuiltinComponent.REG_RST_VALUE_ATTR, 2, Entities::requiresIntegerValue);
        result.setAttributes(attributes);
        result.addInput(newClock(result));
        result.addInput(newWire(result, "rst", BitType.of(1)));
        result.addInput(newWire(result, "i"));
        result.addOutput(newWire(result, "o"));
        return result;
    }

    private ComponentScope createDupComponent(Scope parent, BuiltinRef ref) {
        Utils.requiresMatchedSize(ref.getSourceLoc(), 1, ref.getArgList());
        int dup = requiresIntegerValue(ref.getSourceLoc(), ref.getArgList().get("n", 0)).toInt();

        var component = instantiate(parent, ref);
        var entity = newWire(component, "entity");
        component.addInput(entity);
        for (int i = 0; i < dup; ++i) {
            component.addOutput(entity);
        }
        return component;
    }

    private ComponentScope createSplitComponent(Scope parent, BuiltinRef ref) {
        Utils.requiresMatchedSize(ref.getSourceLoc(), 1, ref.getArgList());
        int split = requiresIntegerValue(ref.getSourceLoc(), ref.getArgList().get("n", 0)).toInt();

        var result = instantiate(parent, ref);
        var entity = newWire(result, "entity", BitType.of(split));
        result.addInput(entity);
        for (int i = split - 1; i >= 0; i--) {
            result.addOutput(Variable.accessOf(entity, Selector.ofBit(i)));
        }
        return result;
    }

    private ComponentScope createCastComponent(Scope parent, BuiltinRef ref) {
        var result = instantiate(parent, ref);
        if (ref.getArgList().isEmpty()) {
            result.addInput(newWire(result, "i"));
            result.addOutput(newWire(result, "o"));
        } else {
            Utils.requiresMatchedSize(ref.getSourceLoc(), 1, ref.getArgList());
            int width = requiresIntegerValue(ref.getSourceLoc(), ref.getArgList().get("width", 0)).toInt();
            result.addInput(newWire(result, "i"));
            result.addOutput(newWire(result, "o", BitType.of(width)));
        }
        return result;
    }

    private ComponentScope createRelationalOpComponent(Scope parent, BuiltinRef ref) {
        Utils.requiresMatchedSize(ref.getSourceLoc(), 0, ref.getArgList());
        var result = instantiate(parent, ref);
        result.addInput(newWire(result, "i1"), newWire(result, "i2"));
        result.addOutput(newWire(result, "o", BitType.forBoolean()));
        return result;
    }

    private ComponentScope createShiftComponent(Scope parent, BuiltinRef ref) {
        Utils.requiresMatchedSize(ref.getSourceLoc(), 0, ref.getArgList());
        var result = instantiate(parent, ref);
        result.addInput(newWire(result, "i"), newWire(result, "off"));
        result.addOutput(newWire(result, "o"));
        return result;
    }

    private ComponentScope createDualInputComponent(Scope parent, BuiltinRef ref) {
        Utils.requiresMatchedSize(ref.getSourceLoc(), 0, ref.getArgList());
        var result = instantiate(parent, ref);
        result.addInput(newWire(result, "i1"), newWire(result, "i2"));
        result.addOutput(newWire(result, "o"));
        return result;
    }

    private ComponentScope createMultiInputComponent(Scope parent, BuiltinRef ref, int argNumHint) {
        int ports;
        if (!ref.getArgList().isEmpty()) {
            Utils.requiresMatchedSize(ref.getSourceLoc(), 1, ref.getArgList());
            ports = requiresIntegerValue(ref.getSourceLoc(), ref.getArgList().get("n", 0)).toInt();
        } else {
            ports = argNumHint;
        }

        if (ports == 1) {
            return createIdentityComponent(parent, BuiltinRef.of(TO, ref.getSourceLoc()));
        } else {
            var result = instantiate(parent, ref);
            for (int i = 1; i <= ports; i++) {
                result.addInput(newWire(result, "i" + i));
            }
            result.addOutput(newWire(result, "o"));
            return result;
        }
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
