package pascal.netx.compiler.elaborator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pascal.netx.compiler.ast.element.InterfaceNode;
import pascal.netx.compiler.exception.CompileCancellationException;
import pascal.netx.compiler.io.Configuration;
import pascal.netx.compiler.model.Utils;
import pascal.netx.compiler.model.scope.ComponentScope;
import pascal.netx.compiler.model.scope.PackageScope;
import pascal.netx.compiler.model.variable.*;
import pascal.netx.compiler.model.variable.type.PolyType;
import pascal.netx.util.lang.Identifier;
import pascal.netx.util.lang.SourceLoc;

import java.util.LinkedHashMap;

public class InterfaceRegister {
    private static final Logger log = LoggerFactory.getLogger(InterfaceRegister.class);

    private final PackageScope pack;

    public InterfaceRegister(PackageScope pack) {
        this.pack = pack;
    }

    public Bundle.Factory register(InterfaceNode node) {
        var scope = new ComponentScope(pack, true, node.getIdentifier());
        var config = Configuration.builder().forInterface().build();
        var elaborator = new ComponentElaborator(config, scope);
        elaborator.buildElements(node.getElements());
        var fields = elaborator.extractIOList(node.getSourceLoc(), node.getType());
        LinkedHashMap<Identifier, Mutable.Factory<?>> factories = new LinkedHashMap<>();
        for (var inst : fields) {
            if (!(inst instanceof Mutable mutable)) {
                log.error("{}: Expected mutable instance but got {}", node.getSourceLoc(), inst);
                throw new CompileCancellationException();
            }
            var factory = getFactoryFrom(node.getSourceLoc(), mutable);
            factories.put(mutable.getIdentifier(), factory);
        }
        return new Bundle.Factory(scope.getIdentifier(), Utils.compilerOf(pack), factories);
    }

    private Mutable.Factory<?> getFactoryFrom(SourceLoc loc, Mutable mutable) {
        return switch (mutable) {
            case Clock _ -> new Clock.Factory(Utils.compilerOf(pack));
            case Wire wire -> {
                if (wire.getType() instanceof PolyType) {
                    log.error("{}: Interface shall not contain auto-typed instance: {}", loc, wire);
                    throw new CompileCancellationException();
                }
                yield new Wire.Factory(wire.getType(), Utils.compilerOf(pack));
            }
            case Bundle bundle -> pack.defs().getInterface(bundle.getType().getIdentifier());
            case Array array -> {
                var elementFactory = getFactoryFrom(loc, array.getElements().getFirst());
                yield new Array.Factory(Utils.compilerOf(pack), array.getElements().size(), elementFactory);
            }
            case Access _ -> {
                log.error("{}: Interface shall not contain data access: {}", loc, mutable);
                throw new CompileCancellationException();
            }
        };
    }
}
