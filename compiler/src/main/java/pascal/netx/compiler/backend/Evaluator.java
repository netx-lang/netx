package pascal.netx.compiler.backend;

import pascal.netx.compiler.io.Logger;
import pascal.netx.compiler.model.ast.expr.*;
import pascal.netx.compiler.model.ast.expr.NamedPortExpr;
import pascal.netx.compiler.model.ast.expr.qualifier.BindingQualifier;
import pascal.netx.compiler.model.ast.expr.qualifier.FilterQualifier;
import pascal.netx.compiler.model.ast.expr.qualifier.GeneratorQualifier;
import pascal.netx.compiler.model.ast.expr.qualifier.Qualifier;
import pascal.netx.compiler.model.circuit.*;
import pascal.netx.compiler.model.circuit.expr.*;
import pascal.netx.compiler.model.circuit.scope.ComponentScope;
import pascal.netx.compiler.model.circuit.scope.PackageScope;
import pascal.netx.compiler.model.circuit.scope.binding.AttrBindList;
import pascal.netx.compiler.model.circuit.scope.binding.IdEntityMap;
import pascal.netx.compiler.model.circuit.scope.binding.OrderedAttrBindList;
import pascal.netx.compiler.model.circuit.signal.*;
import pascal.netx.compiler.model.circuit.signal.selector.BitSelector;
import pascal.netx.compiler.model.circuit.signal.selector.PartSelector;
import pascal.netx.compiler.model.circuit.signal.selector.Selector;
import pascal.netx.compiler.model.type.Hole;
import pascal.netx.compiler.utils.CompileCancellationException;
import pascal.netx.util.lang.Identifier;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;

import static pascal.netx.compiler.backend.Elaborator.evaluateBinding;
import static pascal.netx.compiler.model.circuit.Entities.*;
import static pascal.netx.compiler.model.circuit.Utils.compilerOf;

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
            expr = castToCircuitExpr(evaluate(node), node.getSourceLoc());
            mode = EvaluationMode.COMMON;
        } else {
            expr = castToCircuitExpr(evaluate(node), node.getSourceLoc());
        }
        new Connector(scope).connect(expr);
    }

    public int evaluateToInt(ExprNode expr) {
        return Entities.requireVectorValue(evaluate(expr)).toInt();
    }

    public BooleanValue evaluateAndCastToBoolean(ExprNode expr) {
        return castToBooleanValue(evaluate(expr));
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
            case NamedPortExpr sub -> evaluateNamedPortExpr(sub);
            case ConditionalExpr sub -> evaluateConditionalExpr(sub);
        };
        Logger.exitLoc();
        return result;
    }

    private Entity evaluateFieldExpr(FieldExpr expr) {
        var base = evaluate(expr.getBase());
        if (base instanceof pascal.netx.compiler.model.circuit.scope.Enum enumeration) {
            return enumeration.fields().get(expr.getField());
        }
        if (base instanceof ImportRef ref) {
            var importedEnum = compilerOf(pack).getPackManager().resolveImportedEnum(ref);
            if (importedEnum != null) {
                return importedEnum.fields().get(expr.getField());
            }
        }
        var bundle = requireDataBundle(base);
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

    private Selector evaluateSelector(pascal.netx.compiler.model.ast.expr.selector.Selector selector) {
        return switch (selector) {
            case pascal.netx.compiler.model.ast.expr.selector.BitSelector bitSelector ->
                    new BitSelector(evaluateToInt(bitSelector.getIndex()));
            case pascal.netx.compiler.model.ast.expr.selector.PartSelector partSelector -> new PartSelector(
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
                    for (var entity : requireEList(evaluate(generator.getFrom()))) {
                        scope.overrideBindings(IdEntityMap.of(generator.getHead(), entity));
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
                    scope.overrideBindings(evaluateBinding(binding, scope));
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
        var binding = evaluateBinding(expr.getBinding(), scope);
        scope.overrideBindings(binding);
        var result = evaluate(expr.getBody());
        scope.restoreBindings();
        return result;
    }

    private Entity evaluateNumberExpr(NumberExpr expr) {
        return switch (expr) {
            case BitVectorNumber bitVector -> VectorValue.of(bitVector.getRepresentation());
            case FloatNumber _float -> FloatValue.of(_float.getSize(), _float.getValue());
            case BoolNumber _bool -> BooleanValue.of(_bool.getValue());
            case IntNumber _int -> VectorValue.fromInt(_int.getValue());
        };
    }

    private Entity evaluateStringExpr(StringExpr expr) {
        return StringLiteral.of(expr.getValue());
    }

    private Entity evaluateUnaryExpr(UnaryExpr expr) {
        return switch (expr.getOp()) {
            case POS, NEG, NOT, BITWISE_NOT -> switch (requireValue(evaluate(expr.getOperand()))) {
                case FloatValue _float -> FloatValue.dispatchUnaryOp(expr.getOp()).apply(_float);
                case VectorValue _vector -> VectorValue.dispatchUnaryOp(expr.getOp()).apply(_vector);
                case BooleanValue _boolean -> BooleanValue.dispatchUnaryOp(expr.getOp()).apply(_boolean);
            };
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
            case Signal signal -> DecoratedExpr.of(decorator, SignalRef.of(signal, base.getSourceLoc()));
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
                    evaluateBinaryExpr(expr, expr.getOp());
            case LOGIC_OR, LOGIC_AND ->
                    evaluateBinaryExpr(expr, BooleanValue.dispatchBinaryOp(expr.getOp()));
        };
    }

    private Value evaluateBinaryExpr(BinaryExpr expr, BinaryExpr.Operator op) {
        var lhs = requireValue(evaluate(expr.getLhs()));
        var rhs = requireValue(evaluate(expr.getRhs()));
        if (lhs instanceof FloatValue || rhs instanceof FloatValue) {
            int size = Math.max(
                    lhs instanceof FloatValue(int _size, var _) ? _size : 32,
                    rhs instanceof FloatValue(int _size, var _) ? _size : 32);
            return FloatValue.dispatchBinaryOp(op).apply(castToFloat(size, lhs), castToFloat(size, rhs));
        } else {
            return VectorValue.dispatchBinaryOp(op).apply(requireVectorValue(lhs), requireVectorValue(rhs));
        }
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

    private Entity evaluateNamedPortExpr(NamedPortExpr expr) {
        var ports = new pascal.netx.compiler.model.circuit.expr.NamedPortExpr();
        for (var assign : expr.getPorts()) {
            var rhs = castToCircuitExpr(evaluate(assign.rhs()), expr.getSourceLoc());
            if (assign.isDefault()) {
                ports.setDefaultExpr(rhs);
            } else {
                ports.addPortAssign(assign.lhs(), rhs);
            }
        }
        return ports;
    }

    private Entity evaluateSequentialExpr(BinaryExpr expr) {
        var rhs = castToCircuitExpr(evaluate(expr.getRhs()), expr.getSourceLoc());
        var lhs = castToCircuitExpr(evaluate(expr.getLhs()), expr.getSourceLoc());
        return SequentialExpr.of(lhs, rhs);
    }

    private Entity evaluateSymbolExpr(SymbolExpr expr) {
        return switch (expr) {
            case IdentifierExpr sub -> evaluatePrimarySymbolExpr(sub);
            case CallExpr sub -> evaluateCallExpr(sub);
        };
    }

    private Value evaluateEqualityExpr(BinaryExpr expr) {
        var lhs = requireListedValue(evaluate(expr.getLhs()));
        var rhs = requireListedValue(evaluate(expr.getRhs()));
        boolean isEqual = Utils.isEqualListedValue(lhs, rhs);
        boolean needEqual = expr.getOp() == BinaryExpr.Operator.EQ;
        return BooleanValue.of(needEqual == isEqual);
    }

    public Entity evaluateMacroExpr(Macro macro, AttrBindList parameters) {
        var override = switch (parameters) {
            case IdEntityMap binding -> {
                Utils.requireEqSize(macro.identifiers(), binding.getArguments(),
                        "Macro invocation requires all arguments to be bound");
                for (var id : macro.identifiers()) {
                    if (binding.lookup(id) == null) {
                        Logger.error("Missing macro argument {}", id);
                        throw new CompileCancellationException();
                    }
                }
                yield binding.copyOf();
            }
            case OrderedAttrBindList(var args) -> {
                int size = Utils.requireEqSize(macro.identifiers(), args,
                        "Macro invocation requires all arguments to be bound");
                var bindings = IdEntityMap.ofEmpty();
                for (int i = 0; i < size; i++) {
                    bindings.bind(macro.identifiers().get(i), args.get(i));
                }
                yield bindings;
            }
        };
        var environment = IdEntityMap.ofEmpty();
        macro.captures().forEach(environment::bind);
        if (macro.self() != null) {
            environment.bind(macro.self(), macro);
        }
        override.forEach(entry -> environment.bind(entry.getKey(), entry.getValue()));
        return scope.withLexicalBindings(environment, () -> evaluate(macro.expr()));
    }

    public Entity evaluateSystemMetaExpr(SystemMeta meta, AttrBindList parameters) {
        return meta.apply(parameters);
    }

    @Nonnull Entity resolveIdentifier(Identifier id) {
        var entity = scope.resolveIdentifier(id);
        if (entity == null) {
            switch (mode) {
                case COMMON -> {
                    Logger.error("Cannot resolve identifier " + id);
                    throw new CompileCancellationException();
                }
                case DECLARE_IN_PLACE -> entity = declareDataInPlace(id);
            }
        }
        return entity;
    }

    @Nonnull
    private Entity declareDataInPlace(Identifier id) {
        var factory = new Wire.Factory(new Hole(), compilerOf(pack).getIndexer());
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
            case PrimaryExpr atom -> atom.withArgList(arguments);
            default -> {
                Logger.error("Expected callable but got {}", prototype);
                throw new CompileCancellationException();
            }
        };
    }

    AttrBindList evaluateArguments(CallExpr expr) {
        if (expr.getAssigns().isEmpty() || expr.getAssigns().getFirst().first() == null) {
            var result = OrderedAttrBindList.ofEmpty();
            for (var argument : expr.getArguments()) {
                result.add(evaluate(argument));
            }
            return result;
        } else {
            var result = IdEntityMap.ofEmpty();
            for (var assign : expr.getAssigns()) {
                var argument = evaluate(assign.second());
                result.bind(assign.first(), argument);
            }
            return result;
        }
    }

    private Entity evaluatePrimarySymbolExpr(IdentifierExpr expr) {
        Entity entity = resolveIdentifier(expr.getIdentifier());
        if (entity instanceof Macro macro) {
            return evaluateMacroExpr(macro, AttrBindList.ofEmpty());
        } else if (entity instanceof SystemMeta meta) {
            return evaluateSystemMetaExpr(meta, AttrBindList.ofEmpty());
        }
        return entity;
    }

    enum EvaluationMode {
        COMMON, DECLARE_IN_PLACE
    }
}
