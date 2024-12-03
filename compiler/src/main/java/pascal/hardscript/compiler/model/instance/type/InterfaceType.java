package pascal.hardscript.compiler.model.instance.type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pascal.hardscript.compiler.exception.CompileCancellationException;
import pascal.hardscript.compiler.model.instance.Data;
import pascal.hardscript.compiler.model.scope.ComponentScope;
import pascal.hardscript.util.lang.Identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class InterfaceType implements InstanceType{
    private static final Logger log = LoggerFactory.getLogger(InterfaceType.class);
    private final List<InstanceType> types = new ArrayList<>();
    private final Identifier id;

    private InterfaceType(Identifier id) {
        this.id = id;
    }

    public static InterfaceType of(ComponentScope scope) {
        var result = new InterfaceType(scope.getIdentifier());
        for (var inst : scope.getOutputs()) {
            if (!(inst instanceof Data data)) {
                log.error("{}: Expected data instance, got {}", scope.getSourceLoc(), inst);
                throw new CompileCancellationException();
            }
            result.types.add(data.getType());
        }
        return result;
    }

    public List<InstanceType> getTypes() {
        return Collections.unmodifiableList(types);
    }

    public Identifier getId() {
        return id;
    }
}
