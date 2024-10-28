package pascal.hardscript.compiler.model.components;

import pascal.hardscript.compiler.ast.Identifier;
import pascal.hardscript.compiler.model.Entity;
import pascal.hardscript.compiler.model.scope.Scope;

public interface Component extends Entity {
    Identifier getIdentifier();

    Scope getParent();

    interface Factory<T extends Component> extends Entity {
        T create(Identifier id);
    }
}
