package pascal.hardscript.compiler.elaborator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pascal.hardscript.compiler.ast.component.BindingNode;
import pascal.hardscript.compiler.ast.expr.*;
import pascal.hardscript.compiler.ast.expr.qualifier.BindingQualifier;
import pascal.hardscript.compiler.ast.expr.qualifier.FilterQualifier;
import pascal.hardscript.compiler.ast.expr.qualifier.GeneratorQualifier;
import pascal.hardscript.compiler.ast.expr.qualifier.Qualifier;
import pascal.hardscript.compiler.exception.CompileCancellationException;
import pascal.hardscript.compiler.model.Entity;
import pascal.hardscript.compiler.model.EntityList;
import pascal.hardscript.compiler.model.component.*;
import pascal.hardscript.compiler.model.instance.*;
import pascal.hardscript.compiler.model.instance.selector.BitSelector;
import pascal.hardscript.compiler.model.instance.selector.PartSelector;
import pascal.hardscript.compiler.model.instance.selector.Selector;
import pascal.hardscript.compiler.model.instance.type.BitType;
import pascal.hardscript.compiler.model.instance.type.InstanceType;
import pascal.hardscript.compiler.model.instance.type.PolyType;
import pascal.hardscript.compiler.model.scope.ComponentScope;
import pascal.hardscript.compiler.model.scope.DesignScope;
import pascal.hardscript.compiler.model.scope.Scope;
import pascal.hardscript.compiler.model.scope.binding.Binding;
import pascal.hardscript.compiler.model.scope.binding.MapBinding;
import pascal.hardscript.compiler.model.scope.binding.SingleBinding;
import pascal.hardscript.util.lang.BuiltinComponent;
import pascal.hardscript.util.lang.Identifier;
import pascal.hardscript.util.lang.SourceLoc;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.stream.IntStream;

import static pascal.hardscript.compiler.elaborator.Types.*;

public final class Evaluator {
    private static final Logger log = LoggerFactory.getLogger(Evaluator.class);
    private static final Map<BinaryExpr.Operator, BiFunction<IntegerValue, IntegerValue, Value>>
            BINARY_OPERATOR_MAP = Map.ofEntries(
                    Map.entry(BinaryExpr.Operator.ADD, IntegerValue::add),
                    Map.entry(BinaryExpr.Operator.SUB, IntegerValue::sub),
                    Map.entry(BinaryExpr.Operator.MUL, IntegerValue::mul),
                    Map.entry(BinaryExpr.Operator.DIV, IntegerValue::div),
                    Map.entry(BinaryExpr.Operator.MOD, IntegerValue::mod),
                    Map.entry(BinaryExpr.Operator.POW, IntegerValue::pow),
                    Map.entry(BinaryExpr.Operator.AND, IntegerValue::and),
                    Map.entry(BinaryExpr.Operator.OR, IntegerValue::or),
                    Map.entry(BinaryExpr.Operator.XOR, IntegerValue::xor),
                    Map.entry(BinaryExpr.Operator.XNOR, IntegerValue::xnor),
                    Map.entry(BinaryExpr.Operator.ARITH_SHR, IntegerValue::ashr),
                    Map.entry(BinaryExpr.Operator.LOGIC_SHR, IntegerValue::lshr),
                    Map.entry(BinaryExpr.Operator.ARITH_SHL, IntegerValue::shl),
                    Map.entry(BinaryExpr.Operator.LOGIC_SHL, IntegerValue::shl),
                    Map.entry(BinaryExpr.Operator.EQ, IntegerValue::eq),
                    Map.entry(BinaryExpr.Operator.NE, IntegerValue::ne),
                    Map.entry(BinaryExpr.Operator.LT, IntegerValue::lt),
                    Map.entry(BinaryExpr.Operator.LE, IntegerValue::le),
                    Map.entry(BinaryExpr.Operator.GT, IntegerValue::gt),
                    Map.entry(BinaryExpr.Operator.GE, IntegerValue::ge)
    );

    private final LinkedList<Binding> bindings = new LinkedList<>();
    private final DesignScope global;
    private final Scope scope;

    private EvaluationMode mode = EvaluationMode.COMMON;

    public Evaluator(Scope scope) {
        this.scope = scope;
        this.global = scope.getGlobal();
        bindings.add(scope.getBinding());
    }

    public ComponentType evaluateToComponentType(ExprNode expr) {
        return castToComponentType(evaluate(expr), expr.getSourceLoc());
    }

    public EntityList evaluateToList(ExprNode expr) {
        return requiresList(expr.getSourceLoc(), evaluate(expr));
    }

    public IntegerValue evaluateToIntegerValue(ExprNode expr) {
        return requiresIntegerValue(expr.getSourceLoc(), evaluate(expr));
    }

    public BooleanValue evaluateToBoolean(ExprNode expr) {
        return castToBooleanValue(expr.getSourceLoc(), evaluate(expr));
    }

    public SingleBinding evaluateBinding(BindingNode binding) {
        Identifier id;
        List<Identifier> freeVariables;
        switch (binding.getHead()) {
            case ParamSymbolExpr param -> {
                id = getIdentifierFrom(param.getHead());
                freeVariables = param.getArguments().stream()
                        .map(PrimarySymbol.class::cast)
                        .map(this::getIdentifierFrom).toList();
            }
            case PrimarySymbol primary -> {
                id = getIdentifierFrom(primary);
                freeVariables = List.of();
            }
        }
        return SingleBinding.of(id, Macro.of(freeVariables, binding.getExpr()));
    }

    public Entity evaluateAndDeclInPlace(ExprNode expr) {
        mode = EvaluationMode.DECLARE_IN_PLACE;
        var entity = evaluate(expr);
        mode = EvaluationMode.COMMON;
        return entity;
    }

    public Entity evaluate(ExprNode expr) {
        return switch (expr) {
            case ListExpr sub -> evaluateListExpr(sub);
            case UnaryExpr sub -> evaluateUnaryExpr(sub);
            case AccessExpr sub -> evaluateAccessExpr(sub);
            case BinaryExpr sub -> evaluateBinaryExpr(sub);
            case NumberExpr sub -> evaluateNumberExpr(sub);
            case SymbolExpr sub -> evaluateSymbolExpr(sub);
            case ConditionalExpr sub -> evaluateConditionalExpr(sub);
        };
    }

    private Entity evaluateAccessExpr(AccessExpr expr) {
        var base = evaluate(expr.getBase());
        var selector = evaluateSelector(expr.getSelector());
        return switch (base) {
            case Instance inst -> {
                try {
                    yield Instance.accessOf(inst, selector);
                } catch (CompileCancellationException e) {
                    log.error("{}: invalid selectors {}", expr.getSourceLoc(), expr);
                    throw e;
                }
            }
            case EntityList list -> list.select(selector);
            default -> {
                log.error("{}: Expected component or list but got {}",
                        expr.getBase().getSourceLoc(), base);
                throw new CompileCancellationException();
            }
        };
    }

    private Selector evaluateSelector(pascal.hardscript.compiler.ast.expr.selector.Selector selector) {
        return switch (selector) {
            case pascal.hardscript.compiler.ast.expr.selector.BitSelector bitSelector ->
                    BitSelector.of(evaluateToIntegerValue(bitSelector.getIndex()));
            case pascal.hardscript.compiler.ast.expr.selector.PartSelector partSelector -> PartSelector.of(
                    evaluateToIntegerValue(partSelector.getStart()),
                    evaluateToIntegerValue(partSelector.getEnd()));
        };
    }

    private Entity evaluateListExpr(ListExpr expr) {
        return switch (expr) {
            case CommonList sub -> evaluateCommonList(sub);
            case RangeList sub -> evaluateRangeList(sub);
            case ListComprehension sub -> evaluateListComprehension(sub);
        };
    }

    private EntityList evaluateCommonList(CommonList list) {
        var elements = list.getElements().stream().map(this::evaluate).toList();
        if (elements.isEmpty()) {
            return EntityList.ofEmpty();
        }
        return EntityList.of(elements);
    }

    private EntityList evaluateRangeList(RangeList list) {
        var from = evaluateToIntegerValue(list.getFrom()).toInt();
        var to = evaluateToIntegerValue(list.getTo()).toInt();
        return EntityList.of(
                IntStream.range(Integer.min(from, to), Integer.max(from, to))
                        .mapToObj(Int32Value::of)
                        .map(Entity.class::cast).toList());
    }

    private EntityList evaluateListComprehension(ListComprehension list) {
        return EntityList.of(doListComprehension(list.getHead(), new LinkedList<>(list.getQualifiers())));
    }

    private List<Entity> doListComprehension(ExprNode head, LinkedList<Qualifier> qualifiers) {
        List<Entity> result;
        if (qualifiers.isEmpty()) {
            return List.of(evaluate(head));
        } else {
            switch (qualifiers.pop()) {
                case GeneratorQualifier generator -> {
                    result = new LinkedList<>();
                    var id = getIdentifierFrom(generator.getHead());
                    var list = evaluateToList(generator.getFrom());
                    for (var entity : list.entities()) {
                        overrideBindings(SingleBinding.of(id, entity));
                        result.addAll(doListComprehension(head, new LinkedList<>(qualifiers)));
                        restoreBindings();
                    }
                }
                case FilterQualifier filter -> {
                    var condition = evaluateToBoolean(filter.getCondition());
                    if (condition.value()) {
                        result = doListComprehension(head, new LinkedList<>(qualifiers));
                    } else {
                        result = List.of();
                    }
                }
                case BindingQualifier qualifier -> {
                    var binding = qualifier.getBinding();
                    overrideBindings(evaluateBinding(binding));
                    result = doListComprehension(head, new LinkedList<>(qualifiers));
                    restoreBindings();
                }
            }
        }
        return result;
    }

    private Entity evaluateConditionalExpr(ConditionalExpr expr) {
        var condition = evaluateToBoolean(expr.getCondition());
        return condition.value()
                ? evaluate(expr.getThenBranch())
                : evaluate(expr.getElseBranch());
    }

    private Entity evaluateNumberExpr(NumberExpr expr) {
        return switch (expr) {
            case BitVectorNumber bitVectorNumber -> BitVectorValue.of(bitVectorNumber.getRepresentation());
            case BoolNumber boolNumber -> BooleanValue.of(boolNumber.getValue());
            case IntNumber intNumber -> Int32Value.of(intNumber.getValue());
        };
    }

    private Entity evaluateUnaryExpr(UnaryExpr expr) {
        return switch (expr.getOp()) {
            case ADD -> evaluateToIntegerValue(expr.getOperand());
            case SUB -> evaluateToIntegerValue(expr.getOperand()).neg();
            case NOT -> evaluateToBoolean(expr.getOperand()).not();
            case BITWISE_NOT -> evaluateToIntegerValue(expr.getOperand()).bitwiseNot();
            case SINK -> evaluateUnaryToInstanceType(InstanceAtom.Mode.SINK, expr);
            case INOUT -> evaluateUnaryToInstanceType(InstanceAtom.Mode.BIDI, expr);
        };
    }

    private Entity evaluateUnaryToInstanceType(InstanceAtom.Mode mode, UnaryExpr expr) {
        var operand = requiresMutableInstance(expr.getSourceLoc(), evaluate(expr.getOperand()));
        return InstanceAtom.of(mode, operand, expr.getSourceLoc());
    }

    public Data.Factory<?> evaluateToDataFactory(ExprNode expr) {
        Data.Factory<?> factory;
        if (expr == null) {
            factory = Wire.Factory.of(
                    scope.getLeastUpperComponent(),
                    PolyType.getInstance(), global.getDataIndexer());
        } else {
            mode = EvaluationMode.DATA_FACTORY;
            factory = requiresDataFactory(expr.getSourceLoc(), evaluate(expr));
            mode = EvaluationMode.COMMON;
        }
        return factory;
    }

    private Entity evaluateBinaryExpr(BinaryExpr expr) {
        var sourceLoc = expr.getSourceLoc();
        return switch (expr.getOp()) {
            case COMBINE -> evaluateSequentialType(sourceLoc, evaluate(expr.getLhs()), evaluate(expr.getRhs()));
            case CONCAT -> evaluateListConcat(expr);
            case ADD, SUB, MUL, DIV, MOD, POW, AND, OR, XOR, XNOR,
                 EQ, NE, LT, LE, GT, GE, ARITH_SHR, ARITH_SHL, LOGIC_SHR, LOGIC_SHL ->
                    evaluateBinaryExpr(expr, BINARY_OPERATOR_MAP.get(expr.getOp()));
            case LOGIC_OR -> evaluateBinaryExpr(expr, BooleanValue::or);
            case LOGIC_AND -> evaluateBinaryExpr(expr, BooleanValue::and);
        };
    }

    private Value evaluateBinaryExpr(BinaryExpr expr,
                                     BiFunction<IntegerValue, IntegerValue, Value> op) {
        return op.apply(evaluateToIntegerValue(expr.getLhs()), evaluateToIntegerValue(expr.getRhs()));
    }

    private Value evaluateBinaryExpr(BinaryExpr expr, BinaryOperator<BooleanValue> op) {
        return op.apply(evaluateToBoolean(expr.getLhs()), evaluateToBoolean(expr.getRhs()));
    }

    private EntityList evaluateListConcat(BinaryExpr expr) {
        List<Entity> result = new LinkedList<>();
        var lhs = evaluateToList(expr.getLhs());
        var rhs = evaluateToList(expr.getRhs());
        result.addAll(lhs.entities());
        result.addAll(rhs.entities());
        return EntityList.of(result);
    }

    private Entity evaluateSequentialType(SourceLoc sourceLoc, Entity lhs, Entity rhs) {
        return SequentialType.of(
                castToComponentType(lhs, sourceLoc),
                castToComponentType(rhs, sourceLoc));
    }

    private Wire.Factory evaluateWireFactory(SourceLoc sourceLoc, List<ExprNode> arguments) {
        requiresMatchedSize(sourceLoc, 1, arguments);
        return Wire.Factory.of(
                scope.getLeastUpperComponent(),
                evaluateBitWidth(arguments.getFirst()), global.getDataIndexer());
    }

    private Clock.Factory evaluateClockFactory(SourceLoc sourceLoc, List<ExprNode> arguments) {
        if (arguments.isEmpty()) {
            return Clock.Factory.of(
                    scope.getLeastUpperComponent(),
                    global.getDataIndexer());
        } else {
            log.error("{}: Unexpected specification for component type clock: {}", sourceLoc, arguments);
            throw new CompileCancellationException();
        }
    }

    private InstanceType evaluateBitWidth(ExprNode expr) {
        if (expr instanceof ParamSymbolExpr spec) {
            var identifier = getIdentifierFrom(spec.getHead());
            if (identifier.name().equals("bit")) {
                if (spec.getArguments().size() == 1) {
                    return BitType.of(evaluateToIntegerValue(spec.getArguments().getFirst()).toInt());
                }
            }
        }
        log.error("{}: Unexpected bit type specification: {}", expr.getSourceLoc(), expr);
        throw new CompileCancellationException();
    }

    private Entity evaluateSymbolExpr(SymbolExpr expr) {
        return switch (expr) {
            case PrimarySymbol sub -> evaluatePrimarySymbolExpr(sub);
            case ParamSymbolExpr sub -> evaluateParamSymbolExpr(sub);
        };
    }

    private Entity evaluatePrimarySymbolExpr(PrimarySymbol expr) {
        var id = getIdentifierFrom(expr);
        Entity entity = resolveIdentifier(id);
        if (entity instanceof Macro macro) {
            return evaluateMacroExpr(expr.getSourceLoc(), macro, List.of());
        }
        return entity;
    }

    public Entity evaluateMacroExpr(SourceLoc sourceLoc, Macro marco, List<Entity> parameters) {
        int size = requiresMatchedSize(sourceLoc, marco.identifiers(), parameters);
        var bindings = MapBinding.ofEmpty();
        for (int i = 0; i < size; i++) {
            bindings.bind(marco.identifiers().get(i), parameters.get(i));
        }
        overrideBindings(bindings);
        var result = evaluate(marco.expr());
        restoreBindings();
        return result;
    }

    @Nonnull
    private Entity resolveIdentifier(Identifier id) {
        Entity entity = lookup(id);
        if (entity == null) {
            if (scope instanceof ComponentScope) {
                entity = global.getEvaluator().lookup(id);
                if (entity == null) {
                    if (global.hasDefinition(id)) {
                        entity = ComponentAtom.of(global.getDefinition(id), id);
                    } else if (BuiltinComponent.isBuiltin(id)) {
                        entity = BuiltinAtom.of(BuiltinComponent.of(id), id.getSourceLoc());
                    }
                }
            } else {
                entity = scope.getParent().getEvaluator().resolveIdentifier(id);
            }
        }
        return Objects.requireNonNullElseGet(entity, () -> {
            if (mode == EvaluationMode.DECLARE_IN_PLACE) {
                return declareDataInPlace(id);
            }
            log.error("{}: Unresolved identifier {}", id.getSourceLoc(), id);
            throw new CompileCancellationException();
        });
    }

    @Nonnull
    private Entity declareDataInPlace(Identifier id) {
        var factory = Wire.Factory.of(scope.getLeastUpperComponent(), PolyType.getInstance(), global.getDataIndexer());
        var data = factory.create(id);
        scope.bind(id, data);
        return data;
    }

    @Nullable
    private Entity lookup(Identifier id) {
        for (var binding : bindings) {
            var entity = binding.lookup(id);
            if (entity != null) {
                return entity;
            }
        }
        return null;
    }

    private Entity evaluateParamSymbolExpr(ParamSymbolExpr expr) {
        return switch (mode) {
            case COMMON, DECLARE_IN_PLACE -> evaluateComponentType(expr);
            case DATA_FACTORY -> evaluateDataFactory(expr);
        };
    }

    private Entity evaluateComponentType(ParamSymbolExpr expr) {
        var identifier = getIdentifierFrom(expr.getHead());
        var parameters = evaluateParameters(expr);
        Entity prototype = resolveIdentifier(identifier);
        if (prototype instanceof Macro macro) {
            return evaluateMacroExpr(expr.getSourceLoc(), macro, parameters);
        } else if (prototype instanceof AtomType atom) {
            atom.setParameters(parameters);
            return atom;
        } else {
            log.error("{}: Expected component type but got {}", expr.getSourceLoc(), prototype);
            throw new CompileCancellationException();
        }
    }

    private List<Entity> evaluateParameters(ParamSymbolExpr expr) {
        List<Entity> result = new ArrayList<>();
        for (var arg : expr.getArguments()) {
            var entity = evaluate(arg);
            var param = switch (entity) {
                case ComponentType type -> type;
                case Data ignored -> castToComponentType(entity, arg.getSourceLoc());
                case DataAccess ignored -> castToComponentType(entity, arg.getSourceLoc());
                default -> requiresValue(arg.getSourceLoc(), entity);
            };
            result.add(param);
        }
        return result;
    }


    private Data.Factory<?> evaluateDataFactory(ParamSymbolExpr expr) {
        var identifier = getIdentifierFrom(expr.getHead());
        var sourceLoc = expr.getSourceLoc();
        return switch (identifier.name()) {
            case "wire" -> evaluateWireFactory(sourceLoc, expr.getArguments());
            case "clock" -> evaluateClockFactory(sourceLoc, expr.getArguments());
            default -> {
                log.error("{}: Unknown component type {}", expr.getSourceLoc(), expr);
                throw new CompileCancellationException();
            }
        };
    }

    public Identifier getIdentifierFrom(PrimarySymbol symbol) {
        return switch (symbol) {
            case IdentifierSymbol id -> id.getIdentifier();
            case InterpolatedSymbol interpolated -> evaluateInterpolation(interpolated);
        };
    }

    private Identifier evaluateInterpolation(InterpolatedSymbol interpolated) {
        var sb = new StringBuilder();
        var exprIter = interpolated.getExprs().iterator();
        for (var fragment : interpolated.getFragments()) {
            sb.append(fragment);
            sb.append(evaluate(exprIter.next()));
        }
        exprIter.forEachRemaining(expr -> sb.append(evaluate(expr)));
        return Identifier.of(sb.toString()).setSourceLoc(interpolated.getSourceLoc());
    }

    private void overrideBindings(Binding binding) {
        bindings.addFirst(binding);
    }

    private void restoreBindings() {
        bindings.removeFirst();
    }

    enum EvaluationMode {
        COMMON, DATA_FACTORY, DECLARE_IN_PLACE
    }
}
