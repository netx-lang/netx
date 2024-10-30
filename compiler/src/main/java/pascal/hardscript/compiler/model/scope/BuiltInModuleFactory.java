package pascal.hardscript.compiler.model.scope;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pascal.hardscript.compiler.ast.Identifier;
import pascal.hardscript.compiler.exception.CompileCancellationException;
import pascal.hardscript.compiler.model.components.Wire;
import pascal.hardscript.compiler.model.connection.AtomType;

import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

import static pascal.hardscript.compiler.elaborator.Types.*;

public class BuiltInModuleFactory {
    private static final Logger log = LoggerFactory.getLogger(BuiltInModuleFactory.class);
    private static final Map<String, BiFunction<Scope, AtomType, ModuleScope>> BUILTIN_FACTORY = Map.of(
            "TO", BuiltInModuleFactory::createTOModule,
            "OR", BuiltInModuleFactory::createORModule,
            "STORE", BuiltInModuleFactory::createSTOREModule,
            "SPLIT", BuiltInModuleFactory::createSPLITModule,
            "NOT", BuiltInModuleFactory::createNOTModule,
            "AND", BuiltInModuleFactory::createANDModule,
            "XOR", BuiltInModuleFactory::createXORModule,
            "NOR", BuiltInModuleFactory::createNORModule,
            "NAND", BuiltInModuleFactory::createNANDModule
    );
    public static final Set<String> BUILTIN = BUILTIN_FACTORY.keySet();

    public static ModuleScope createBuiltInModule(Scope parent, AtomType type) {
        var identifier = type.identifier();
        var factory = BUILTIN_FACTORY.get(identifier.string());
        if (factory == null) {
            log.error("{}: Unknown built-in module: {}", identifier.getSourceLoc(), identifier);
            throw new CompileCancellationException();
        }
        return factory.apply(parent, type);
    }

    private static ModuleScope createNOTModule(Scope parent, AtomType type) {
        var module = parent.instantiateModuleScope(parent.getNameAllocator().allocateFor(type));
        module.addInput(newWire(module, "input", 1));
        module.addOutput(newWire(module, "output", 1));
        return module;
    }

    private static ModuleScope createTOModule(Scope parent, AtomType type) {
        var module = parent.instantiateModuleScope(parent.getNameAllocator().allocateFor(type));
        var entity = newWire(module, "entity", 1);
        module.addInput(entity);
        module.addOutput(entity);
        return module;
    }

    private static ModuleScope createSTOREModule(Scope parent, AtomType type) {
        var sourceLoc = type.identifier().getSourceLoc();
        requiresMatchedSize(sourceLoc, 1, type.parameters());
        var register = requiresRegister(sourceLoc, type.parameters().getFirst());
        var module = parent.instantiateModuleScope(parent.getNameAllocator().allocateFor(type));
        var input = newWire(module, "input", register.width());
        module.addInput(input);
        module.alias(input, register);
        module.addOutput(register);
        return module;
    }

    private static ModuleScope createSPLITModule(Scope parent, AtomType type) {
        var sourceLoc = type.identifier().getSourceLoc();
        requiresMatchedSize(sourceLoc, 1, type.parameters());
        int split = requiresIntegerValue(sourceLoc, type.parameters().getFirst()).toInt();

        var module = parent.instantiateModuleScope(parent.getNameAllocator().allocateFor(type));
        var entity = newWire(module, "entity", 1);
        module.addInput(entity);
        for (int i = 0; i < split; ++i) {
            module.addOutput(entity);
        }
        return module;
    }

    private static ModuleScope createANDModule(Scope parent, AtomType type) {
        return makeDualInputGateModule(parent, type);
    }

    private static ModuleScope createORModule(Scope parent, AtomType type) {
        return makeDualInputGateModule(parent, type);
    }

    private static ModuleScope createXORModule(Scope parent, AtomType type) {
        return makeDualInputGateModule(parent, type);
    }

    private static ModuleScope createNANDModule(Scope parent, AtomType type) {
        return makeDualInputGateModule(parent, type);
    }

    private static ModuleScope createNORModule(Scope parent, AtomType type) {
        return makeDualInputGateModule(parent, type);
    }

    private static ModuleScope makeDualInputGateModule(Scope parent, AtomType type) {
        int ports;
        if (type.parameters().isEmpty()) {
            ports = 2;
        } else {
            var sourceLoc = type.identifier().getSourceLoc();
            requiresMatchedSize(sourceLoc, 1, type.parameters());
            ports = requiresIntegerValue(sourceLoc, type.parameters().getFirst()).toInt();
        }

        var module = parent.instantiateModuleScope(parent.getNameAllocator().allocateFor(type));
        for (int i = 1; i <= ports; i++) {
            module.addInput(newWire(module, "input" + i, 1));
        }
        module.addOutput(newWire(module, "output", 1));
        return module;
    }

    private static Wire newWire(ModuleScope module, String name, int size) {
        var wire = Wire.of(Identifier.of("$" + name), module, size);
        module.bind(wire.getIdentifier(), wire);
        return wire;
    }
}
