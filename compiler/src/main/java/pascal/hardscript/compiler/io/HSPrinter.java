package pascal.hardscript.compiler.io;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import pascal.hardscript.compiler.exception.CompileCancellationException;
import pascal.hardscript.compiler.model.scope.DesignScope;

public final class HSPrinter {
    private final DesignScope design;

    public HSPrinter(DesignScope design) {
        this.design = design;
    }

    public String dump() {
        // TODO:
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        try {
            return ow.writeValueAsString(design);
        } catch (JsonProcessingException e) {
            throw new CompileCancellationException();
        }
    }
}
