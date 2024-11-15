package pascal.hardscript.compiler.io;

import com.fasterxml.jackson.databind.JsonNode;
import pascal.hardscript.compiler.model.scope.DesignScope;

import static pascal.hardscript.compiler.Configuration.FULLY_EXPAND;

public class HSDumper {
    private final DesignScope design;

    public HSDumper(DesignScope scope) {
        this.design = scope;
    }

    public JsonNode dump() {
        if (FULLY_EXPAND) {
            return new ExpandedDumper(design).dump();
        } else {
            return new CompactDumper(design).dump();
        }
    }
}
