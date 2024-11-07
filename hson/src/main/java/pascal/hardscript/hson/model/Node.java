package pascal.hardscript.hson.model;

import pascal.hardscript.util.lang.Identifier;

public interface Node {
    Identifier getIdentifier();

    enum Type {
        COMPONENT, CLOCK, WIRE
    }
}
