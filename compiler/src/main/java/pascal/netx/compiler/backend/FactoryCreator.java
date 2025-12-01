package pascal.netx.compiler.backend;

import pascal.netx.compiler.NXCompiler;
import pascal.netx.compiler.io.Logger;
import pascal.netx.compiler.model.ast.element.DataInstantiationNode;
import pascal.netx.compiler.model.ast.element.InterfaceNode;
import pascal.netx.compiler.model.ast.expr.CallExpr;
import pascal.netx.compiler.model.ast.expr.ExprNode;
import pascal.netx.compiler.model.ast.expr.IdentifierExpr;
import pascal.netx.compiler.model.circuit.Utils;
import pascal.netx.compiler.model.circuit.scope.Attributes;
import pascal.netx.compiler.model.circuit.scope.ComponentScope;
import pascal.netx.compiler.model.circuit.scope.binding.AttrBindList;
import pascal.netx.compiler.model.circuit.signal.*;
import pascal.netx.compiler.model.type.*;
import pascal.netx.compiler.utils.CompileCancellationException;
import pascal.netx.util.lang.Identifier;

import java.util.LinkedHashMap;
import java.util.List;

import static pascal.netx.compiler.model.circuit.Utils.compilerOf;

public final class FactoryCreator {
    private final ComponentScope scope;
    private final Evaluator evaluator;
    private final NXCompiler.WireIndexer indexer;

    public FactoryCreator(ComponentScope scope) {
        this.scope = scope;
        this.evaluator = scope.getEvaluator();
        this.indexer = compilerOf(scope).getIndexer();
    }

    public Data.Factory<?> createFor(DataInstantiationNode node) {
        Data.Factory<?> factory;
        if (node.isAutoFactory()) {
            factory = new Wire.Factory(new Hole(), indexer);
        } else {
            factory = switch (node.getFactory()) {
                case CallExpr expr -> createFromCallExpr(expr);
                case IdentifierExpr expr -> createFromIdentifierExpr(expr);
            };
        }
        checkFactoryLegality(factory.getType(), node);
        return factory;
    }

    public Bundle.Factory createFor(InterfaceNode node) {
        var elaborator = new Elaborator.ComponentElaborator(scope);
        node.getElements().forEach(elaborator::buildElement);
        var fields = elaborator.extractIOList(node.getSignature());
        LinkedHashMap<Identifier, Data.Factory<?>> factories = new LinkedHashMap<>();
        for (var inst : fields) {
            if (!(inst instanceof Data data)) {
                Logger.error("Expected mutable instance but got {}", inst);
                throw new CompileCancellationException();
            }
            var factory = getFactoryFrom(data);
            factories.put(data.getIdentifier(), factory);
        }
        return new Bundle.Factory(node.getIdentifier(), indexer, factories);
    }

    private Data.Factory<?> createFromIdentifierExpr(IdentifierExpr expr) {
        var id = expr.getIdentifier();

        if (id.name().equals("clock")) {
            return createClockFactory(List.of());
        }

        var pack = scope.getPackage();
        if (pack.defs().hasInterface(id)) {
            return createBundleFactory(pack.defs().getInterface(id), AttrBindList.ofEmpty());
        } else if (pack.defs().hasEnum(id)) {
            return new Wire.Factory(BitType.of(pack.defs().getEnum(id).width()), indexer);
        } else {
            Logger.error("Unknown interface type {}", id);
            throw new CompileCancellationException();
        }
    }

    private Data.Factory<?> createFromCallExpr(CallExpr expr) {
        return switch (expr.getHead().name()) {
            case "bit" -> createWireFactory(expr.getArguments());
            case "clock" -> createClockFactory(expr.getArguments());
            default -> {
                var pack = scope.getPackage();
                if (pack.defs().hasInterface(expr.getHead())) {
                    var def = pack.defs().getInterface(expr.getHead());
                    yield createBundleFactory(def, evaluator.evaluateArguments(expr));
                }
                Logger.error("Unknown data type {}", expr);
                throw new CompileCancellationException();
            }
        };
    }

    private Wire.Factory createWireFactory(List<ExprNode> arguments) {
        Utils.requireEqSize(1, arguments, "Wire factory requires one argument");
        var bitType = BitType.of(evaluator.evaluateToInt(arguments.getFirst()));
        return new Wire.Factory(bitType, indexer);
    }

    private Clock.Factory createClockFactory(List<ExprNode> arguments) {
        if (arguments.isEmpty()) {
            return new Clock.Factory(indexer);
        } else {
            Logger.error("Unexpected specification for component type clock: {}", arguments);
            throw new CompileCancellationException();
        }
    }

    Bundle.Factory createBundleFactory(InterfaceNode node, AttrBindList arguments) {
        var component = ComponentScope.newInterface(scope, node.getIdentifier(), false);
        var attributes = Attributes.from(node, arguments);
        for (var bind : attributes.entrySet()) {
            component.bind(bind.getKey(), bind.getValue());
        }
        return new FactoryCreator(component).createFor(node);
    }

    private void checkFactoryLegality(Type type, DataInstantiationNode node) {
        boolean isBundle = switch (node.getKind()) {
            case BUNDLE -> true;
            case WIRE -> false;
        };

        while (type instanceof ArrayType arrayType) {
            type = arrayType.baseType();
        }
        if (isBundle) {
            if (!(type instanceof InterfaceType)) {
                Logger.error("Expected bundle type but got {}", type);
                throw new CompileCancellationException();
            }
        } else {
            if (type instanceof InterfaceType) {
                Logger.error("Expected wire type but got {}", type);
                throw new CompileCancellationException();
            }
        }
    }

    private Data.Factory<?> getFactoryFrom(Data data) {
        return switch (data) {
            case Clock _ -> new Clock.Factory(indexer);
            case Wire wire -> {
                if (wire.getType() instanceof Hole) {
                    Logger.error("Interface shall not contain auto-typed instance: {}", wire);
                    throw new CompileCancellationException();
                }
                yield new Wire.Factory(wire.getType(), indexer);
            }
            case Bundle bundle -> {
                LinkedHashMap<Identifier, Data.Factory<?>> factories = new LinkedHashMap<>();
                for (var field : bundle.getFields().entrySet()) {
                    factories.put(field.getKey(), getFactoryFrom(field.getValue()));
                }
                yield new Bundle.Factory(bundle.getIdentifier(), indexer, factories);
            }
            case Array array -> {
                var elementFactory = getFactoryFrom(array.getElements().getFirst());
                yield new Array.Factory(indexer, array.getElements().size(), elementFactory);
            }
            case Access _ -> {
                Logger.error("Interface shall not contain data access: {}", data);
                throw new CompileCancellationException();
            }
        };
    }
}
