package pascal.netx.compiler.backend;

import pascal.netx.compiler.NXCompiler;
import pascal.netx.compiler.circuit.Entities;
import pascal.netx.compiler.circuit.Entity;
import pascal.netx.compiler.circuit.Utils;
import pascal.netx.compiler.circuit.expr.BuiltinRef;
import pascal.netx.compiler.circuit.scope.Attributes;
import pascal.netx.compiler.circuit.scope.ComponentScope;
import pascal.netx.compiler.circuit.scope.Scope;
import pascal.netx.compiler.circuit.signal.Clock;
import pascal.netx.compiler.circuit.signal.Signal;
import pascal.netx.compiler.circuit.signal.Wire;
import pascal.netx.compiler.circuit.signal.selector.Selector;
import pascal.netx.compiler.circuit.signal.type.BitType;
import pascal.netx.compiler.circuit.signal.type.PolyType;
import pascal.netx.compiler.circuit.signal.type.Type;
import pascal.netx.util.lang.BuiltinComponent;
import pascal.netx.util.lang.Identifier;

import java.util.function.Function;

import static pascal.netx.compiler.circuit.Entities.requiresVectorValue;
import static pascal.netx.util.lang.BuiltinComponent.REG_CLK_EDGE_ATTR;
import static pascal.netx.util.lang.BuiltinComponent.TO;

record BuiltInComponentFactory(NXCompiler.WireIndexer indexer) {
    private static void moveAttribute(Attributes attr, BuiltinRef ref, String name, int index,
                                      Function<Entity, ? extends Entity> checker) {
        attr.addAttribute(Identifier.of(name), checker.apply(ref.getArgList().get(name, index)));
    }

    public ComponentScope createBuiltInComponent(Scope parent, BuiltinRef ref, int argNumHint) {
        if (BuiltinComponent.MULTI_INPUT.contains(ref.getBuiltin())) {
            return createMultiInputComponent(parent, ref, argNumHint);
        } else {
            return createBuiltInComponent(parent, ref);
        }
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
            case OR, AND, XOR, NOR, NAND, XNOR, CONCAT, ADD, MUL -> createMultiInputComponent(parent, ref, 2);
            case SUB, DIV, MOD -> createDualInputComponent(parent, ref);
            case EQ, NE, SLT, SLE, SGT, SGE, ULT, ULE, UGT, UGE -> createRelationalOpComponent(parent, ref);
            case SHL, ASHR, LSHR -> createShiftComponent(parent, ref);
        };
    }

    private ComponentScope createRegisterComponent(Scope parent, BuiltinRef ref) {
        Utils.requireEqSize(1, ref.getArgList(),
                "Register component requires only one boolean argument for clock edge");
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
        Utils.requireEqSize(3, ref.getArgList(),
                "AsyncRegister component requires three arguments: " +
                        "boolean clock edge, boolean reset edge, integer reset value");
        var result = instantiate(parent, ref);
        var attributes = new Attributes();
        moveAttribute(attributes, ref, REG_CLK_EDGE_ATTR, 0, Entities::requiresBooleanValue);
        moveAttribute(attributes, ref, BuiltinComponent.REG_RST_EDGE_ATTR, 1, Entities::requiresBooleanValue);
        moveAttribute(attributes, ref, BuiltinComponent.REG_RST_VALUE_ATTR, 2, Entities::requiresVectorValue);
        result.setAttributes(attributes);
        result.addInput(newClock(result));
        result.addInput(newWire(result, "rst", BitType.of(1)));
        result.addInput(newWire(result, "i"));
        result.addOutput(newWire(result, "o"));
        return result;
    }

    private ComponentScope createDupComponent(Scope parent, BuiltinRef ref) {
        Utils.requireEqSize(1, ref.getArgList(),
                "Dup component requires only one integer argument for number of outputs");
        int dup = requiresVectorValue(ref.getArgList().get("n", 0)).toInt();

        var component = instantiate(parent, ref);
        var entity = newWire(component, "entity");
        component.addInput(entity);
        for (int i = 0; i < dup; ++i) {
            component.addOutput(entity);
        }
        return component;
    }

    private ComponentScope createSplitComponent(Scope parent, BuiltinRef ref) {
        Utils.requireEqSize(1, ref.getArgList(),
                "Split component requires only one integer argument for input bit width");
        int split = requiresVectorValue(ref.getArgList().get("n", 0)).toInt();

        var result = instantiate(parent, ref);
        var entity = newWire(result, "entity", BitType.of(split));
        result.addInput(entity);
        for (int i = split - 1; i >= 0; i--) {
            result.addOutput(Signal.accessOf(entity, Selector.ofBit(i)));
        }
        return result;
    }

    private ComponentScope createCastComponent(Scope parent, BuiltinRef ref) {
        var result = instantiate(parent, ref);
        if (ref.getArgList().isEmpty()) {
            result.addInput(newWire(result, "i"));
            result.addOutput(newWire(result, "o"));
        } else {
            Utils.requireEqSize(1, ref.getArgList(),
                    "Cast component requires one integer argument for bit width");
            int width = requiresVectorValue(ref.getArgList().get("width", 0)).toInt();
            result.addInput(newWire(result, "i"));
            result.addOutput(newWire(result, "o", BitType.of(width)));
        }
        return result;
    }

    private ComponentScope createRelationalOpComponent(Scope parent, BuiltinRef ref) {
        Utils.requireEqSize(0, ref.getArgList(),
                "Relational operator component requires no argument");
        var result = instantiate(parent, ref);
        result.addInput(newWire(result, "i1"), newWire(result, "i2"));
        result.addOutput(newWire(result, "o", BitType.forBoolean()));
        return result;
    }

    private ComponentScope createShiftComponent(Scope parent, BuiltinRef ref) {
        Utils.requireEqSize(0, ref.getArgList(),
                "Shift operator component requires no argument");
        var result = instantiate(parent, ref);
        result.addInput(newWire(result, "i"), newWire(result, "off"));
        result.addOutput(newWire(result, "o"));
        return result;
    }

    private ComponentScope createDualInputComponent(Scope parent, BuiltinRef ref) {
        Utils.requireEqSize(0, ref.getArgList(), ref.getBuiltin() + "requires no argument");
        var result = instantiate(parent, ref);
        result.addInput(newWire(result, "i1"), newWire(result, "i2"));
        result.addOutput(newWire(result, "o"));
        return result;
    }

    private ComponentScope createMultiInputComponent(Scope parent, BuiltinRef ref, int argNumHint) {
        int ports;
        if (!ref.getArgList().isEmpty()) {
            Utils.requireEqSize(1, ref.getArgList(),
                    "Multi-input component requires only one integer argument for number of inputs");
            ports = requiresVectorValue(ref.getArgList().get("n", 0)).toInt();
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
        var clock = new Clock.Factory(indexer).create(result, id);
        result.bind(clock.getIdentifier(), clock);
        return clock;
    }

    private Wire newWire(ComponentScope result, String name, Type type) {
        var id = Identifier.of("$" + name).setSourceLoc(result.getSourceLoc());
        var wire = new Wire.Factory(type, indexer).create(result, id);
        result.bind(wire.getIdentifier(), wire);
        return wire;
    }
}
