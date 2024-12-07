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
import pascal.hardscript.compiler.model.instance.type.PolyType;
import pascal.hardscript.compiler.model.scope.ComponentScope;
import pascal.hardscript.compiler.model.scope.GlobalScope;
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
    private final GlobalScope global;
    private final Scope scope;

    private EvaluationMode mode = EvaluationMode.COMMON;

    public Evaluator(Scope scope) {
        this.scope = scope;
        this.global = scope.getGlobal();
        bindings.add(scope.getBinding());
    }

    public ComponentType evaluateToComponentType(ExprNode expr, boolean needDeclareInPlace) {
        if (needDeclareInPlace) {
            return castToComponentType(evaluateAndDeclInPlace(expr), expr.getSourceLoc());
        }
        return castToComponentType(evaluate(expr), expr.getSourceLoc());
    }

    public EntityList evaluateToList(ExprNode expr) {
        return requiresList(expr.getSourceLoc(), evaluate(expr));
    }

    public IntegerValue evaluateToIntegerValue(ExprNode expr) {
        return requiresIntegerValue(expr.getSourceLoc(), evaluate(expr));
    }

    public int evaluateToInteger(ExprNode expr) {
        return evaluateToIntegerValue(expr).toInt();
    }

    public BooleanValue evaluateToBoolean(ExprNode expr) {
        return castToBooleanValue(expr.getSourceLoc(), evaluate(expr));
    }

    public DataBundle evaluateDataBundle(ExprNode expr) {
        return requiresDataBundle(expr.getSourceLoc(), evaluate(expr));
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
            case FieldExpr sub -> evaluateFieldExpr(sub);
            case AccessExpr sub -> evaluateAccessExpr(sub);
            case BinaryExpr sub -> evaluateBinaryExpr(sub);
            case NumberExpr sub -> evaluateNumberExpr(sub);
            case SymbolExpr sub -> evaluateSymbolExpr(sub);
            case ConditionalExpr sub -> evaluateConditionalExpr(sub);
        };
    }

    private Entity evaluateFieldExpr(FieldExpr expr) {
        var bundle = evaluateDataBundle(expr.getBase());
        var field = getIdentifierFrom(expr.getField());
        return bundle.getField(field);
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
        var from = evaluateToInteger(list.getFrom());
        var to = evaluateToInteger(list.getTo());
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
            case SINK -> evaluateDecoratedInstanceAtom(InstanceAtom.Mode.SINK, expr);
            case SOURCE -> evaluateDecoratedInstanceAtom(InstanceAtom.Mode.SOURCE, expr);
        };
    }

    private Entity evaluateDecoratedInstanceAtom(InstanceAtom.Mode mode, UnaryExpr expr) {
        var operand = evaluate(expr.getOperand());
        switch (operand) {
            case EntityList(var list) -> {
                List<ComponentType> result = new LinkedList<>();
                for (var element : list) {
                    var inst = requiresMutableInstance(expr.getSourceLoc(), element);
                    result.add(InstanceAtom.of(mode, inst, expr.getSourceLoc()));
                }
                return ParallelType.from(result);
            }
            case MutableInstance inst -> {
                return InstanceAtom.of(mode, inst, expr.getSourceLoc());
            }
            default -> {
                log.error("{}: Expected mutable instance(s) but got {}", expr.getSourceLoc(), operand);
                throw new CompileCancellationException();
            }
        }
    }

    public MutableInstance.Factory<?> evaluateToInstanceFactory(ExprNode expr) {
        MutableInstance.Factory<?> factory;
        if (expr == null) {
            factory = new Wire.Factory(PolyType.getInstance(), global);
        } else {
            mode = EvaluationMode.FACTORY;
            factory = requiresInstanceFactory(expr.getSourceLoc(), evaluate(expr));
            mode = EvaluationMode.COMMON;
        }
        return factory;
    }

    private Entity evaluateBinaryExpr(BinaryExpr expr) {
        return switch (expr.getOp()) {
            case COMBINE -> evaluateSequentialType(expr, false);
            case TIE -> evaluateSequentialType(expr, true);
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

    private Entity evaluateSequentialType(BinaryExpr expr, boolean isTied) {
        var leftLoc = expr.getLhs().getSourceLoc();
        var rightLoc = expr.getRhs().getSourceLoc();
        ComponentType lhs, rhs = castToComponentType(evaluate(expr.getRhs()), rightLoc);
        if (isTied) {
            var lhsEntity = evaluate(expr.getLhs());
            if (lhsEntity instanceof EntityList(var list)) {
                return ParallelType.from(list.stream()
                        .map(element -> requiresDataBundle(leftLoc, element))
                        .map(ComponentType.class::cast).toList());
            } else {
                lhs = InstanceAtom.of(InstanceAtom.Mode.BIDI,
                        requiresMutableInstance(leftLoc, lhsEntity), leftLoc);
            }
            return SequentialType.ofTied(lhs, rhs);
        } else {
            lhs = castToComponentType(evaluate(expr.getLhs()), leftLoc);
        }
        return SequentialType.ofSplit(lhs, rhs);
    }

    private Entity evaluateSymbolExpr(SymbolExpr expr) {
        return switch (expr) {
            case PrimarySymbol sub -> evaluatePrimarySymbolExpr(sub);
            case ParamSymbolExpr sub -> evaluateParamSymbolExpr(sub);
        };
    }

    private Entity evaluatePrimarySymbolExpr(PrimarySymbol expr) {
        var id = getIdentifierFrom(expr);
        if (mode == EvaluationMode.FACTORY) {
            if (!scope.getGlobal().hasInterface(id)) {
                log.error("{}: Unknown interface type {}", expr.getSourceLoc(), id);
                throw new CompileCancellationException();
            }
            return scope.getGlobal().getInterface(id);
        } else {
            Entity entity = resolveIdentifier(id);
            if (entity instanceof Macro macro) {
                return evaluateMacroExpr(expr.getSourceLoc(), macro, List.of());
            }
            return entity;
        }
    }

    private Entity evaluateParamSymbolExpr(ParamSymbolExpr expr) {
        if (mode == EvaluationMode.FACTORY) {
            return evaluateInstanceFactory(expr);
        } else {
            return evaluateComponentType(expr);
        }
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
            entity = global.getEvaluator().lookup(id);
            if (entity == null) {
                if (global.hasComponent(id)) {
                    entity = ComponentAtom.of(global.getComponent(id), id);
                } else if (BuiltinComponent.isBuiltin(id)) {
                    entity = BuiltinAtom.of(BuiltinComponent.of(id), id.getSourceLoc());
                }
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
        var factory = new Wire.Factory(PolyType.getInstance(), global);
        var data = factory.create((ComponentScope) scope, id);
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


    private MutableInstance.Factory<?> evaluateInstanceFactory(ParamSymbolExpr expr) {
        var identifier = getIdentifierFrom(expr.getHead());
        var sourceLoc = expr.getSourceLoc();
        return switch (identifier.name()) {
            case "bit" -> evaluateWireFactory(sourceLoc, expr.getArguments());
            case "array" -> evaluateArrayFactory(sourceLoc, expr.getArguments());
            case "clock" -> evaluateClockFactory(sourceLoc, expr.getArguments());
            default -> {
                log.error("{}: Unknown component type {}", expr.getSourceLoc(), expr);
                throw new CompileCancellationException();
            }
        };
    }

    private Wire.Factory evaluateWireFactory(SourceLoc sourceLoc, List<ExprNode> arguments) {
        requiresMatchedSize(sourceLoc, 1, arguments);
        mode = EvaluationMode.COMMON;
        var bitType = BitType.of(evaluateToInteger(arguments.getFirst()));
        mode = EvaluationMode.FACTORY;
        return new Wire.Factory(bitType, global);
    }

    private Array.Factory evaluateArrayFactory(SourceLoc sourceLoc, List<ExprNode> arguments) {
        if (arguments.size() != 2) {
            log.error("{}: Unexpected specification for component type array: {}", sourceLoc, arguments);
            throw new CompileCancellationException();
        }
        var factory = evaluateToInstanceFactory(arguments.get(0));
        int size = evaluateToInteger(arguments.get(1));
        return new Array.Factory(global, size, factory);
    }

    private Clock.Factory evaluateClockFactory(SourceLoc sourceLoc, List<ExprNode> arguments) {
        if (arguments.isEmpty()) {
            return new Clock.Factory(global);
        } else {
            log.error("{}: Unexpected specification for component type clock: {}", sourceLoc, arguments);
            throw new CompileCancellationException();
        }
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

    public void overrideBindings(Binding binding) {
        bindings.addFirst(binding);
    }

    public void restoreBindings() {
        bindings.removeFirst();
    }

    enum EvaluationMode {
        COMMON, FACTORY, DECLARE_IN_PLACE
    }
}
