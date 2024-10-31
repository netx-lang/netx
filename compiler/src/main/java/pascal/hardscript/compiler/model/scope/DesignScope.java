package pascal.hardscript.compiler.model.scope;

import pascal.hardscript.compiler.ast.Identifier;

public final class DesignScope extends Scope {
    private final ComponentInfo componentInfo;

    DesignScope(Scope parent, ComponentInfo componentInfo) {
        super(parent);
        this.componentInfo = componentInfo;
    }

    public ComponentInfo getComponentInfo() {
        return componentInfo;
    }

    public boolean containsInfo(Identifier identifier) {
        return componentInfo.contains(identifier);
    }

    public static DesignScope newDesign(ComponentInfo componentInfo) {
        return new DesignScope(null, componentInfo);
    }
}
