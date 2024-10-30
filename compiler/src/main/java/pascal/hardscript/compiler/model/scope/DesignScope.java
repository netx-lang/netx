package pascal.hardscript.compiler.model.scope;

import pascal.hardscript.compiler.ast.Identifier;

public final class DesignScope extends Scope {
    private final ModuleInfo moduleInfo;

    DesignScope(Scope parent, ModuleInfo moduleInfo) {
        super(parent);
        this.moduleInfo = moduleInfo;
    }

    public ModuleInfo getModuleInfo() {
        return moduleInfo;
    }

    public boolean containsInfo(Identifier identifier) {
        return moduleInfo.contains(identifier);
    }

    public static DesignScope newDesign(ModuleInfo moduleInfo) {
        return new DesignScope(null, moduleInfo);
    }
}
