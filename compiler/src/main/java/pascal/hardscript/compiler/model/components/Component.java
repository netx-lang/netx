package pascal.hardscript.compiler.model.components;

import pascal.hardscript.compiler.ast.Identifier;
import pascal.hardscript.compiler.model.Entity;

public interface Component extends Entity {

    interface Factory<T extends Component> {
        T create(Identifier id);
    }
}
