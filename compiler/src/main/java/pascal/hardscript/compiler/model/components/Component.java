package pascal.hardscript.compiler.model.components;

import pascal.hardscript.compiler.ast.Identifier;
import pascal.hardscript.compiler.model.Entity;

public interface Component extends Entity {

    Identifier getIdentifier();

    interface Factory<T extends Component> extends Entity {
        T create(Identifier id);
    }
}
