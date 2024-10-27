package pascal.hardscript.compiler.model.circuit;

import pascal.hardscript.compiler.ast.Identifier;

public interface Component extends Circuit {

    interface Factory<T extends Component> {
        T create(Identifier id);
    }
}
