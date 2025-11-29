package pascal.netx.compiler.backend;

import pascal.netx.compiler.analysis.ast.FreeVariableDetector;
import pascal.netx.compiler.ast.element.BindingNode;
import pascal.netx.compiler.ast.element.InterfaceNode;
import pascal.netx.compiler.ast.expr.*;
import pascal.netx.compiler.ast.expr.qualifier.BindingQualifier;
import pascal.netx.compiler.ast.expr.qualifier.FilterQualifier;
import pascal.netx.compiler.ast.expr.qualifier.GeneratorQualifier;
import pascal.netx.compiler.ast.expr.qualifier.Qualifier;
import pascal.netx.compiler.circuit.*;
import pascal.netx.compiler.circuit.expr.*;
import pascal.netx.compiler.circuit.scope.Attributes;
import pascal.netx.compiler.circuit.scope.ComponentScope;
import pascal.netx.compiler.circuit.scope.PackageScope;
import pascal.netx.compiler.circuit.scope.binding.AttrBindList;
import pascal.netx.compiler.circuit.scope.binding.MonoBinding;
import pascal.netx.compiler.circuit.scope.binding.MultiBinding;
import pascal.netx.compiler.circuit.scope.binding.OrderedAttrBindList;
import pascal.netx.compiler.circuit.signal.*;
import pascal.netx.compiler.circuit.signal.selector.BitSelector;
import pascal.netx.compiler.circuit.signal.selector.PartSelector;
import pascal.netx.compiler.circuit.signal.selector.Selector;
import pascal.netx.compiler.circuit.signal.type.BitType;
import pascal.netx.compiler.circuit.signal.type.PolyType;
import pascal.netx.compiler.utils.CompileCancellationException;
import pascal.netx.compiler.utils.Logger;
import pascal.netx.util.lang.Identifier;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.IntStream;

import static pascal.netx.compiler.circuit.Entities.*;
import static pascal.netx.compiler.circuit.Utils.compilerOf;
import static pascal.netx.compiler.circuit.signal.VectorValue.dispatchUnaryOp;

public final class Evaluator {
    private final PackageScope pack;
    private final ComponentScope scope;

    private EvaluationMode mode = EvaluationMode.COMMON;

    public Evaluator(ComponentScope component) {
        this.scope = component;
        this.pack = component.getPackage();
    }

    public void evaluate(ExprNode node, boolean needDeclareInPlace) {
        Expr expr;
        if (needDeclareInPlace) {
            mode = EvaluationMode.DECLARE_IN_PLACE;
            expr = castToCircuitExpr(evaluate(node));
            mode = EvaluationMode.COMMON;
        } else {
            expr = castToCircuitExpr(evaluate(node));
        }
        new Connector(scope).connect(expr);
    }

    public int evaluateToInt(ExprNode expr) {
        return evaluateToVectorValue(expr).toInt();
    }

    private VectorValue evaluateToVectorValue(ExprNode expr) {
        return requiresVectorValue(evaluate(expr));
    }

    public BooleanValue evaluateAndCastToBoolean(ExprNode expr) {
        return castToBooleanValue(evaluate(expr));
    }

    public MonoBinding evaluateBinding(BindingNode binding) {
        Identifier id;
        List<Identifier> freeVariables;
        switch (binding.getHead()) {
            case CallExpr param -> {
                id = param.getHead();
                freeVariables = param.getArguments().stream()
                        .map(IdentifierExpr.class::cast)
                        .map(IdentifierExpr::getIdentifier).toList();
            }
            case IdentifierExpr primary -> {
                id = primary.getIdentifier();
                freeVariables = List.of();
            }
        }
        var macro = Macro.of(freeVariables, binding.getExpr());
        checkClosedMacro(id, macro);
        return MonoBinding.of(id, macro);
    }

    private void checkClosedMacro(Identifier name, Macro macro) {
        var freeVariables = FreeVariableDetector.detect(macro.expr());
        macro.identifiers().forEach(freeVariables::remove);
        for (var id : freeVariables) {
            if (!name.equals(id)) { // Not a recursive macro
                if (scope.resolveIdentifier(id) == null) {
                    Logger.error("Macro {} is not closed, missing variable {}", macro.expr(), id);
                    throw new CompileCancellationException();
                }
            }
        }
    }

    public Entity evaluate(ExprNode expr) {
        Logger.enterLoc(expr.getSourceLoc());
        var result = switch (expr) {
            case LetExpr sub -> evaluateLetExpr(sub);
            case ListExpr sub -> evaluateListExpr(sub);
            case UnaryExpr sub -> evaluateUnaryExpr(sub);
            case FieldExpr sub -> evaluateFieldExpr(sub);
            case AccessExpr sub -> evaluateAccessExpr(sub);
            case BinaryExpr sub -> evaluateBinaryExpr(sub);
            case NumberExpr sub -> evaluateNumberExpr(sub);
            case StringExpr sub -> evaluateStringExpr(sub);
            case SymbolExpr sub -> evaluateSymbolExpr(sub);
            case PortConnectionExpr sub -> evaluatePortExpr(sub);
            case ConditionalExpr sub -> evaluateConditionalExpr(sub);
        };
        Logger.exitLoc();
        return result;
    }

    public Data.Factory<?> evaluateToMutableFactory(ExprNode expr) {
        Data.Factory<?> factory;
        if (expr == null) {
            factory = new Wire.Factory(PolyType.getInstance(), compilerOf(pack).getIndexer());
        } else {
            mode = EvaluationMode.FACTORY;
            factory = requiresMutableFactory(evaluate(expr));
            mode = EvaluationMode.COMMON;
        }
        return factory;
    }

    private Entity evaluateFieldExpr(FieldExpr expr) {
        if (expr.getBase() instanceof IdentifierExpr idExpr) {
            var id = idExpr.getIdentifier();
            if (pack.defs().hasEnum(id)) {
                return pack.defs().getEnum(id).fields().get(expr.getField());
            }
        }
        var bundle = requiresDataBundle(evaluate(expr.getBase()));
        return bundle.getField(expr.getField());
    }

    private Entity evaluateAccessExpr(AccessExpr expr) {
        var base = evaluate(expr.getBase());
        var selector = evaluateSelector(expr.getSelector());
        return switch (base) {
            case Signal inst -> {
                try {
                    yield Signal.accessOf(inst, selector);
                } catch (CompileCancellationException e) {
                    Logger.error("invalid selectors {}", expr);
                    throw e;
                }
            }
            case SignalRef ref -> {
                try {
                    yield Signal.accessOf(ref.getInstance(), selector);
                } catch (CompileCancellationException e) {
                    Logger.error("invalid selectors {}", expr);
                    throw e;
                }
            }
            case EList list -> selector.applyOn(list);
            default -> {
                Logger.error("Expected variable or list but got {}", base);
                throw new CompileCancellationException();
            }
        };
    }

    private Selector evaluateSelector(pascal.netx.compiler.ast.expr.selector.Selector selector) {
        return switch (selector) {
            case pascal.netx.compiler.ast.expr.selector.BitSelector bitSelector ->
                    new BitSelector(evaluateToInt(bitSelector.getIndex()));
            case pascal.netx.compiler.ast.expr.selector.PartSelector partSelector -> new PartSelector(
                            evaluateToInt(partSelector.getStart()),
                            evaluateToInt(partSelector.getEnd()));
        };
    }

    private Entity evaluateListExpr(ListExpr expr) {
        return switch (expr) {
            case CommonList sub -> evaluateCommonList(sub);
            case RangeList sub -> evaluateRangeList(sub);
            case ListComprehension sub -> evaluateListComprehension(sub);
        };
    }

    private EList evaluateCommonList(CommonList list) {
        var elements = list.getElements().stream().map(this::evaluate).toList();
        if (elements.isEmpty()) {
            return EList.ofEmpty();
        }
        return EList.of(elements);
    }

    private EList evaluateRangeList(RangeList list) {
        var from = evaluateToInt(list.getFrom());
        var to = evaluateToInt(list.getTo());
        var result = new ArrayList<>(
                IntStream.range(Integer.min(from, to), Integer.max(from, to))
                        .mapToObj(VectorValue::fromInt)
                        .map(Entity.class::cast)
                        .toList());
        if (from > to) {
            Collections.reverse(result);
        }
        return EList.of(result);
    }

    private EList evaluateListComprehension(ListComprehension list) {
        return doListComprehension(list.getHead(), new LinkedList<>(list.getQualifiers()));
    }

    private EList doListComprehension(ExprNode head, LinkedList<Qualifier> qualifiers) {
        EList result = new EList();
        if (qualifiers.isEmpty()) {
            return EList.of(evaluate(head));
        } else {
            switch (qualifiers.pop()) {
                case GeneratorQualifier generator -> {
                    for (var entity : requiresEList(evaluate(generator.getFrom()))) {
                        scope.overrideBindings(MonoBinding.of(generator.getHead(), entity));
                        result.addAll(doListComprehension(head, new LinkedList<>(qualifiers)));
                        scope.restoreBindings();
                    }
                }
                case FilterQualifier filter -> {
                    var condition = evaluateAndCastToBoolean(filter.getCondition());
                    if (condition.value()) {
                        result.addAll(doListComprehension(head, new LinkedList<>(qualifiers)));
                    }
                }
                case BindingQualifier qualifier -> {
                    var binding = qualifier.getBinding();
                    scope.overrideBindings(evaluateBinding(binding));
                    result.addAll(doListComprehension(head, new LinkedList<>(qualifiers)));
                    scope.restoreBindings();
                }
            }
        }
        return result;
    }

    private Entity evaluateConditionalExpr(ConditionalExpr expr) {
        var condition = evaluateAndCastToBoolean(expr.getCondition());
        return condition.value()
                ? evaluate(expr.getThenBranch())
                : evaluate(expr.getElseBranch());
    }

    private Entity evaluateLetExpr(LetExpr expr) {
        var binding = evaluateBinding(expr.getBinding());
        scope.overrideBindings(binding);
        var result = evaluate(expr.getBody());
        scope.restoreBindings();
        return result;
    }

    private Entity evaluateNumberExpr(NumberExpr expr) {
        return switch (expr) {
            case BitVectorNumber bitVectorNumber -> VectorValue.of(bitVectorNumber.getRepresentation());
            case BoolNumber boolNumber -> BooleanValue.of(boolNumber.getValue());
            case IntNumber intNumber -> VectorValue.fromInt(intNumber.getValue());
        };
    }

    private Entity evaluateStringExpr(StringExpr expr) {
        return StringLiteral.of(expr.getValue());
    }

    private Entity evaluateUnaryExpr(UnaryExpr expr) {
        return switch (expr.getOp()) {
            case POS, NEG, NOT, BITWISE_NOT
                    -> dispatchUnaryOp(expr.getOp()).apply(evaluateToVectorValue(expr.getOperand()));
            case BLOCK_OUT -> evaluateDecoratedExpr(DecoratedExpr.Decorator.BLOCK_OUT, expr.getOperand());
            case BLOCK_IN -> evaluateDecoratedExpr(DecoratedExpr.Decorator.BLOCK_IN, expr.getOperand());
        };
    }

    private Entity evaluateDecoratedExpr(DecoratedExpr.Decorator decorator, ExprNode base) {
        var operand = evaluate(base);
        return switch (operand) {
            case EList list -> {
                List<Expr> result = new LinkedList<>();
                for (var element : list) {
                    if (!(element instanceof Expr expr)) {
                        Logger.error("Expected instantiate expr but got {}", element);
                        throw new CompileCancellationException();
                    }
                    result.add(DecoratedExpr.of(decorator, expr));
                }
                yield ParallelExpr.from(result);
            }
            case Expr expr -> DecoratedExpr.of(decorator, expr);
            case Signal signal -> DecoratedExpr.of(decorator, SignalRef.of(signal));
            default -> {
                Logger.error("Expected instantiate expr(s) but got {}", operand);
                throw new CompileCancellationException();
            }
        };
    }

    private Entity evaluateBinaryExpr(BinaryExpr expr) {
        return switch (expr.getOp()) {
            case SEQ -> evaluateSequentialExpr(expr);
            case PAR -> evaluateListExpr(CommonList.of(List.of(expr.getLhs(), expr.getRhs())));
            case CONCAT -> evaluateListConcat(expr);
            case EQ, NE -> evaluateEqualityExpr(expr);
            case ADD, SUB, MUL, DIV, MOD, POW, AND, OR, XOR, XNOR,
                 ULT, ULE, UGT, UGE, SLT, SLE, SGT, SGE,
                 ARITH_SHR, ARITH_SHL, LOGIC_SHR, LOGIC_SHL ->
                    evaluateBinaryExpr(expr, VectorValue.dispatchBinaryOp(expr.getOp()));
            case LOGIC_OR, LOGIC_AND ->
                    evaluateBinaryExpr(expr, BooleanValue.dispatchBinaryOp(expr.getOp()));
        };
    }

    private Value evaluateBinaryExpr(BinaryExpr expr, VectorValue.BinaryOp op) {
        return op.apply(evaluateToVectorValue(expr.getLhs()), evaluateToVectorValue(expr.getRhs()));
    }

    private BooleanValue evaluateBinaryExpr(BinaryExpr expr, BooleanValue.BinaryOp op) {
        return op.apply(evaluateAndCastToBoolean(expr.getLhs()), evaluateAndCastToBoolean(expr.getRhs()));
    }

    private EList evaluateListConcat(BinaryExpr expr) {
        List<Entity> result = new LinkedList<>();
        result.addAll(castToEList(evaluate(expr.getLhs())));
        result.addAll(castToEList(evaluate(expr.getRhs())));
        return EList.of(result);
    }

    private Entity evaluatePortExpr(PortConnectionExpr expr) {
        var ports = new NamedPortExpr();
        for (var assign : expr.getPorts()) {
            var rhs = castToCircuitExpr(evaluate(assign.rhs()));
            if (assign.isDefault()) {
                ports.setDefaultExpr(rhs);
            } else {
                ports.addPortAssign(assign.lhs(), rhs);
            }
        }
        return ports;
    }

    private Entity evaluateSequentialExpr(BinaryExpr expr) {
        var rhs = castToCircuitExpr(evaluate(expr.getRhs()));
        var lhs = castToCircuitExpr(evaluate(expr.getLhs()));
        return SequentialExpr.of(lhs, rhs);
    }

    private Entity evaluateSymbolExpr(SymbolExpr expr) {
        return switch (expr) {
            case IdentifierExpr sub -> evaluatePrimarySymbolExpr(sub);
            case CallExpr sub -> switch (mode) {
                case FACTORY -> evaluateVariableFactory(sub);
                case COMMON, DECLARE_IN_PLACE -> evaluateCallExpr(sub);
            };
        };
    }

    private Value evaluateEqualityExpr(BinaryExpr expr) {
        var lhs = requiresListedValue(evaluate(expr.getLhs()));
        var rhs = requiresListedValue(evaluate(expr.getRhs()));
        boolean isEqual = Utils.isEqualListedValue(lhs, rhs);
        boolean needEqual = expr.getOp() == BinaryExpr.Operator.EQ;
        return BooleanValue.of(needEqual == isEqual);
    }

    public Entity evaluateMacroExpr(Macro marco, AttrBindList parameters) {
        var override = switch (parameters) {
            case MultiBinding binding -> binding.copyOf();
            case OrderedAttrBindList(var args) -> {
                int size = Utils.requireEqSize(marco.identifiers(), args,
                        "Macro invocation requires all arguments to be bound");
                var bindings = MultiBinding.ofEmpty();
                for (int i = 0; i < size; i++) {
                    bindings.bind(marco.identifiers().get(i), args.get(i));
                }
                yield bindings;
            }
        };
        scope.overrideBindings(override);
        var result = evaluate(marco.expr());
        scope.restoreBindings();
        return result;
    }

    public Entity evaluateSystemMetaExpr(SystemMeta meta, AttrBindList parameters) {
        return meta.apply(parameters);
    }

    @Nonnull Entity resolveIdentifier(Identifier id) {
        var entity = scope.resolveIdentifier(id);
        return Objects.requireNonNullElseGet(entity, () -> {
            if (mode == Evaluator.EvaluationMode.DECLARE_IN_PLACE) {
                return declareDataInPlace(id);
            }
            Logger.error("Unresolved identifier {}", id);
            throw new CompileCancellationException();
        });
    }

    @Nonnull
    private Entity declareDataInPlace(Identifier id) {
        var factory = new Wire.Factory(PolyType.getInstance(), compilerOf(pack).getIndexer());
        var data = factory.create(scope, id);
        scope.bind(id, data);
        return data;
    }

    private Entity evaluateCallExpr(CallExpr expr) {
        Entity prototype = resolveIdentifier(expr.getHead());
        AttrBindList arguments = evaluateArguments(expr);
        return switch (prototype) {
            case Macro macro -> evaluateMacroExpr(macro, arguments);
            case SystemMeta systemMeta -> evaluateSystemMetaExpr(systemMeta, arguments);
            case PrimaryExpr atom -> {
                atom.setArgList(arguments);
                yield atom;
            }
            default -> {
                Logger.error("Expected callable but got {}", prototype);
                throw new CompileCancellationException();
            }
        };
    }

    private AttrBindList evaluateArguments(CallExpr expr) {
        if (expr.getAssigns().isEmpty() || expr.getAssigns().getFirst().first() == null) {
            var result = OrderedAttrBindList.ofEmpty();
            for (var argument : expr.getArguments()) {
                result.add(evaluate(argument));
            }
            return result;
        } else {
            var result = MultiBinding.ofEmpty();
            for (var assign : expr.getAssigns()) {
                var argument = evaluate(assign.second());
                result.bind(assign.first(), argument);
            }
            return result;
        }
    }

    private Entity evaluatePrimarySymbolExpr(IdentifierExpr expr) {
        var id = expr.getIdentifier();
        if (mode == EvaluationMode.FACTORY) {
            if (id.name().equals("clock")) {
                return evaluateClockFactory(List.of());
            }

            var pack = scope.getPackage();
            if (pack.defs().hasInterface(id)) {
                return evaluateInterfaceFactory(
                        pack.defs().getInterface(id), AttrBindList.ofEmpty());
            } else if (pack.defs().hasEnum(id)) {
                return new Wire.Factory(BitType.of(pack.defs().getEnum(id).width()), compilerOf(pack).getIndexer());
            } else {
                Logger.error("Unknown interface type {}", id);
                throw new CompileCancellationException();
            }
        } else {
            Entity entity = resolveIdentifier(id);
            if (entity instanceof Macro macro) {
                return evaluateMacroExpr(macro, AttrBindList.ofEmpty());
            } else if (entity instanceof SystemMeta meta) {
                return evaluateSystemMetaExpr(meta, AttrBindList.ofEmpty());
            }
            return entity;
        }
    }

    private Data.Factory<?> evaluateVariableFactory(CallExpr expr) {
        return switch (expr.getHead().name()) {
            case "bit" -> evaluateWireFactory(expr.getArguments());
            case "clock" -> evaluateClockFactory(expr.getArguments());
            default -> {
                if (pack.defs().hasInterface(expr.getHead())) {
                    var def = pack.defs().getInterface(expr.getHead());
                    yield evaluateInterfaceFactory(def, evaluateArguments(expr));
                }
                Logger.error("Unknown data type {}", expr);
                throw new CompileCancellationException();
            }
        };
    }

    private Bundle.Factory evaluateInterfaceFactory(InterfaceNode node, AttrBindList arguments) {
        var component = ComponentScope.newInterface(scope, node.getIdentifier(), false);
        var attributes = Attributes.from(node, arguments);
        for (var bind : attributes.entrySet()) {
            component.bind(bind.getKey(), bind.getValue());
        }
        return new InterfaceFactoryCreator(component).create(node);
    }

    private Wire.Factory evaluateWireFactory(List<ExprNode> arguments) {
        Utils.requireEqSize(1, arguments, "Wire factory requires one argument");
        mode = EvaluationMode.COMMON;
        var bitType = BitType.of(evaluateToInt(arguments.getFirst()));
        mode = EvaluationMode.FACTORY;
        return new Wire.Factory(bitType, compilerOf(pack).getIndexer());
    }

    private Clock.Factory evaluateClockFactory(List<ExprNode> arguments) {
        if (arguments.isEmpty()) {
            return new Clock.Factory(compilerOf(pack).getIndexer());
        } else {
            Logger.error("Unexpected specification for component type clock: {}", arguments);
            throw new CompileCancellationException();
        }
    }

    enum EvaluationMode {
        COMMON, FACTORY, DECLARE_IN_PLACE
    }

}
