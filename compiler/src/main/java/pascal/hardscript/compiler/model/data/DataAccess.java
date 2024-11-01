package pascal.hardscript.compiler.model.data;

import pascal.hardscript.compiler.ast.Identifier;
import pascal.hardscript.compiler.model.data.selector.Selector;
import pascal.hardscript.compiler.model.scope.Scope;

public record DataAccess(Data base, Selector selector) implements Data {

    public static DataAccess of(Data base, Selector selector) {
        return new DataAccess(base, selector);
    }

    @Override
    public Identifier getIdentifier() {
        return base.getIdentifier();
    }

    @Override
    public Scope getParent() {
        // FIXME: check
        return base.getParent();
    }

    @Override
    public DataType getType() {
        return BitType.of(selector.getWidth());
    }
}
