package pascal.netx.compiler.elaborator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pascal.netx.compiler.ast.element.ComponentNode;
import pascal.netx.compiler.exception.CompileCancellationException;
import pascal.netx.compiler.io.Configuration;
import pascal.netx.compiler.model.instance.*;
import pascal.netx.compiler.model.instance.type.PolyType;
import pascal.netx.compiler.model.scope.ComponentScope;
import pascal.netx.compiler.model.scope.PackageScope;
import pascal.netx.util.lang.Identifier;

import java.util.LinkedHashMap;

public class InterfaceElaborator {
    private static final Logger log = LoggerFactory.getLogger(InterfaceElaborator.class);

    private final PackageScope pack;

    public InterfaceElaborator(PackageScope pack) {
        this.pack = pack;
    }

    public DataBundle.Factory elaborate(ComponentNode node) {
        var scope = ComponentScope.newInterface(pack, node.getIdentifier());
        new ComponentElaborator(Configuration.forExpanded(), scope).build(node);

        LinkedHashMap<Identifier, MutableInstance.Factory<?>> factories = new LinkedHashMap<>();
        for (var inst : scope.getOutputs()) {
            if (!(inst instanceof MutableInstance mutable)) {
                log.error("{}: Expected mutable instance but got {}", node.getSourceLoc(), inst);
                throw new CompileCancellationException();
            }
            var factory = getFactoryFrom(node, mutable);
            factories.put(mutable.getIdentifier(), factory);
        }
        return new DataBundle.Factory(scope.getIdentifier(), pack, factories);
    }

    private MutableInstance.Factory<?> getFactoryFrom(ComponentNode node, MutableInstance mutable) {
        return switch (mutable) {
            case Clock _ -> new Clock.Factory(pack);
            case Wire wire -> {
                if (wire.getType() instanceof PolyType) {
                    log.error("{}: Interface shall not contain auto-typed instance: {}", node.getSourceLoc(), wire);
                    throw new CompileCancellationException();
                }
                yield new Wire.Factory(wire.getType(), pack);
            }
            case DataBundle bundle -> pack.getInterface(bundle.getType().getIdentifier());
            case Array array -> {
                var elementFactory = getFactoryFrom(node, array.getElements().getFirst());
                yield new Array.Factory(pack, array.getElements().size(), elementFactory);
            }
            case DataAccess _ -> {
                log.error("{}: Interface shall not contain data access: {}", node.getSourceLoc(), mutable);
                throw new CompileCancellationException();
            }
        };
    }
}
