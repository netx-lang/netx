package pascal.netx.compiler.elaborator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pascal.netx.compiler.ast.element.InterfaceNode;
import pascal.netx.compiler.exception.CompileCancellationException;
import pascal.netx.compiler.io.Configuration;
import pascal.netx.compiler.io.NXCompiler;
import pascal.netx.compiler.model.Utils;
import pascal.netx.compiler.model.scope.ComponentScope;
import pascal.netx.compiler.model.variable.*;
import pascal.netx.compiler.model.variable.type.PolyType;
import pascal.netx.util.lang.Identifier;
import pascal.netx.util.lang.SourceLoc;

import java.util.LinkedHashMap;

public class InterfaceFactoryMaker {
    private static final Logger log = LoggerFactory.getLogger(InterfaceFactoryMaker.class);

    private final ComponentScope scope;
    private final NXCompiler compiler;

    public InterfaceFactoryMaker(ComponentScope scope) {
        this.scope = scope;
        this.compiler = Utils.compilerOf(scope);
    }

    public Bundle.Factory make(InterfaceNode node) {
        var config = Configuration.builder().build();
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
        return new Bundle.Factory(scope.getIdentifier(), compiler, factories);
    }

    private Mutable.Factory<?> getFactoryFrom(SourceLoc loc, Mutable mutable) {
        return switch (mutable) {
            case Clock _ -> new Clock.Factory(compiler);
            case Wire wire -> {
                if (wire.getType() instanceof PolyType) {
                    log.error("{}: Interface shall not contain auto-typed instance: {}", loc, wire);
                    throw new CompileCancellationException();
                }
                yield new Wire.Factory(wire.getType(), compiler);
            }
            case Bundle bundle -> {
                LinkedHashMap<Identifier, Mutable.Factory<?>> factories = new LinkedHashMap<>();
                for (var field : bundle.getFields().entrySet()) {
                    factories.put(field.getKey(), getFactoryFrom(loc, field.getValue()));
                }
                yield new Bundle.Factory(bundle.getIdentifier(), compiler, factories);
            }
            case Array array -> {
                var elementFactory = getFactoryFrom(loc, array.getElements().getFirst());
                yield new Array.Factory(compiler, array.getElements().size(), elementFactory);
            }
            case Access _ -> {
                log.error("{}: Interface shall not contain data access: {}", loc, mutable);
                throw new CompileCancellationException();
            }
        };
    }
}
