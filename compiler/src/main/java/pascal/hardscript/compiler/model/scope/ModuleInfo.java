package pascal.hardscript.compiler.model.scope;

import pascal.hardscript.compiler.ast.Identifier;
import pascal.hardscript.compiler.ast.module.ModuleNode;

import java.util.*;

import static pascal.hardscript.compiler.model.scope.BuiltInModuleFactory.BUILTIN;

public final class ModuleInfo {
    private final Set<Identifier> modules = new HashSet<>();
    private final Set<Identifier> builtin = new HashSet<>();
    private final Map<Identifier, ModuleNode> moduleNodes = new HashMap<>();

    public ModuleInfo(List<ModuleNode> modules) {
        for (var node : modules) {
            this.modules.add(node.getIdentifier());
            this.moduleNodes.put(node.getIdentifier(), node);
        }
        BUILTIN.stream().map(Identifier::of).forEach(this.builtin::add);
    }

    public boolean contains(Identifier identifier) {
        return containsModule(identifier) || containsBuiltin(identifier);
    }

    public boolean containsModule(Identifier identifier) {
        return modules.contains(identifier);
    }

    public boolean containsBuiltin(Identifier identifier) {
        return builtin.contains(identifier);
    }

    public ModuleNode getModuleNode(Identifier identifier) {
        return moduleNodes.get(identifier);
    }

    public static ModuleInfo of(List<ModuleNode> modules) {
        return new ModuleInfo(modules);
    }
}
