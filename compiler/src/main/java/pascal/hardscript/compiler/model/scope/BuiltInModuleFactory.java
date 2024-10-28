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

public class BuiltInModuleFactory {
    private static final Logger log = LoggerFactory.getLogger(BuiltInModuleFactory.class);
    private static final Map<String, BiFunction<Scope, Identifier, ModuleScope>> BUILTIN_FACTORY = Map.of(
            "TO", BuiltInModuleFactory::createTOModule,
            "OR", BuiltInModuleFactory::createORModule,
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
        return factory.apply(parent, identifier);
    }

    private static ModuleScope createNOTModule(Scope parent, Identifier id) {
        var module = parent.instantiateModuleScope(id);
        var input = Wire.of(Identifier.of("input"), module, 1);
        var output = Wire.of(Identifier.of("output"), module, 1);
        module.bind(input.getIdentifier(), input);
        module.bind(output.getIdentifier(), output);
        module.addInput(input);
        module.addOutput(output);
        return module;
    }

    private static ModuleScope createTOModule(Scope parent, Identifier id) {
        var module = parent.instantiateModuleScope(id);
        var entity = Wire.of(Identifier.of("entity"), module, 1);
        module.bind(entity.getIdentifier(), entity);
        module.addInput(entity);
        module.addOutput(entity);
        return module;
    }

    private static ModuleScope createANDModule(Scope parent, Identifier id) {
        return makeDualInputGateModule(parent, id);
    }

    private static ModuleScope createORModule(Scope parent, Identifier id) {
        return makeDualInputGateModule(parent, id);
    }

    private static ModuleScope createXORModule(Scope parent, Identifier id) {
        return makeDualInputGateModule(parent, id);
    }

    private static ModuleScope createNANDModule(Scope parent, Identifier id) {
        return makeDualInputGateModule(parent, id);
    }

    private static ModuleScope createNORModule(Scope parent, Identifier id) {
        return makeDualInputGateModule(parent, id);
    }

    private static ModuleScope makeDualInputGateModule(Scope parent, Identifier id) {
        var module = parent.instantiateModuleScope(id);
        var input1 = Wire.of(Identifier.of("input1"), module, 1);
        var input2 = Wire.of(Identifier.of("input2"), module, 1);
        var output = Wire.of(Identifier.of("output"), module, 1);
        module.bind(input1.getIdentifier(), input1);
        module.bind(input2.getIdentifier(), input2);
        module.bind(output.getIdentifier(), output);
        module.addInput(input1, input2);
        module.addOutput(output);
        return module;
    }
}
