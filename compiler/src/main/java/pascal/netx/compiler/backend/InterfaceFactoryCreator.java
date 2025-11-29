package pascal.netx.compiler.backend;

import pascal.netx.compiler.NXCompiler;
import pascal.netx.compiler.ast.element.InterfaceNode;
import pascal.netx.compiler.circuit.Utils;
import pascal.netx.compiler.circuit.scope.ComponentScope;
import pascal.netx.compiler.circuit.signal.*;
import pascal.netx.compiler.circuit.signal.type.PolyType;
import pascal.netx.compiler.io.Logger;
import pascal.netx.compiler.utils.CompileCancellationException;
import pascal.netx.util.lang.Identifier;

import java.util.LinkedHashMap;

final class InterfaceFactoryCreator {
    private final ComponentScope scope;
    private final NXCompiler compiler;

    public InterfaceFactoryCreator(ComponentScope scope) {
        this.scope = scope;
        this.compiler = Utils.compilerOf(scope);
    }

    public Bundle.Factory create(InterfaceNode node) {
        var elaborator = new Elaborator.ComponentElaborator(scope);
        node.getElements().forEach(elaborator::buildElement);
        var fields = elaborator.extractIOList(node.getSig());
        LinkedHashMap<Identifier, Data.Factory<?>> factories = new LinkedHashMap<>();
        for (var inst : fields) {
            if (!(inst instanceof Data data)) {
                Logger.error("Expected mutable instance but got {}", inst);
                throw new CompileCancellationException();
            }
            var factory = getFactoryFrom(data);
            factories.put(data.getIdentifier(), factory);
        }
        return new Bundle.Factory(scope.getIdentifier(), compiler.getIndexer(), factories);
    }

    private Data.Factory<?> getFactoryFrom(Data data) {
        return switch (data) {
            case Clock _ -> new Clock.Factory(compiler.getIndexer());
            case Wire wire -> {
                if (wire.getType() instanceof PolyType) {
                    Logger.error("Interface shall not contain auto-typed instance: {}", wire);
                    throw new CompileCancellationException();
                }
                yield new Wire.Factory(wire.getType(), compiler.getIndexer());
            }
            case Bundle bundle -> {
                LinkedHashMap<Identifier, Data.Factory<?>> factories = new LinkedHashMap<>();
                for (var field : bundle.getFields().entrySet()) {
                    factories.put(field.getKey(), getFactoryFrom(field.getValue()));
                }
                yield new Bundle.Factory(bundle.getIdentifier(), compiler.getIndexer(), factories);
            }
            case Array array -> {
                var elementFactory = getFactoryFrom(array.getElements().getFirst());
                yield new Array.Factory(compiler.getIndexer(), array.getElements().size(), elementFactory);
            }
            case Access _ -> {
                Logger.error("Interface shall not contain data access: {}", data);
                throw new CompileCancellationException();
            }
        };
    }
}
