package pascal.hardscript.compiler.ast.module.type;

import pascal.hardscript.compiler.ast.ASTListener;

public final class ClockType extends ComponentTypeNode {
    private static final ClockType INSTANCE = new ClockType();

    private ClockType() {
    }

    public static ClockType getInstance() {
        return INSTANCE;
    }

    @Override
    public void walk(ASTListener listener) {
        listener.enter(this);
        listener.exit(this);
    }
}
