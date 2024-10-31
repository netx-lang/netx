package pascal.hardscript.compiler.elaborator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pascal.hardscript.compiler.ast.Identifier;
import pascal.hardscript.compiler.ast.SourceLoc;
import pascal.hardscript.compiler.ast.expr.*;
import pascal.hardscript.compiler.ast.expr.qualifier.BindingQualifier;
import pascal.hardscript.compiler.ast.expr.qualifier.FilterQualifier;
import pascal.hardscript.compiler.ast.expr.qualifier.GeneratorQualifier;
import pascal.hardscript.compiler.ast.expr.qualifier.Qualifier;
import pascal.hardscript.compiler.exception.CompileCancellationException;
import pascal.hardscript.compiler.model.Entity;
import pascal.hardscript.compiler.model.ListEntity;
import pascal.hardscript.compiler.model.components.Clock;
import pascal.hardscript.compiler.model.components.Component;
import pascal.hardscript.compiler.model.components.ComponentAccess;
import pascal.hardscript.compiler.model.components.Wire;
import pascal.hardscript.compiler.model.components.selector.BitSelector;
import pascal.hardscript.compiler.model.components.selector.PartSelector;
import pascal.hardscript.compiler.model.components.selector.Selector;
import pascal.hardscript.compiler.model.connection.*;
import pascal.hardscript.compiler.model.scope.DesignScope;
import pascal.hardscript.compiler.model.scope.Scope;
import pascal.hardscript.compiler.model.scope.SingleBinding;
import pascal.hardscript.compiler.model.value.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import static pascal.hardscript.compiler.elaborator.Types.*;

public final class Evaluator {
    private static final Logger log = LoggerFactory.getLogger(Evaluator.class);
    private final LinkedList<pascal.hardscript.compiler.model.scope.Binding> bindings = new LinkedList<>();
    private EvaluationMode mode = EvaluationMode.COMMON;
    private final Scope scope;

    public Evaluator(Scope scope) {
        this.scope = scope;
        bindings.add(scope.getBinding());
    }

    public InstanceType evaluateToInstanceType(ExprNode expr) {
        return requiresInstanceType(expr.getSourceLoc(), evaluate(expr));
    }

    public Port evaluateToPort(ExprNode expr) {
        return requiresPort(expr.getSourceLoc(), evaluate(expr));
    }

    public ListEntity evaluateToList(ExprNode expr) {
        return requiresList(expr.getSourceLoc(), evaluate(expr));
    }

    public IntegerValue evaluateToIntegerValue(ExprNode expr) {
        return requiresIntegerValue(expr.getSourceLoc(), evaluate(expr));
    }

    public BooleanValue evaluateToBoolean(ExprNode expr) {
        return castToBooleanValue(expr.getSourceLoc(), evaluate(expr));
    }

    public Component.Factory<?> evaluateToComponentFactory(ExprNode expr) {
        mode = EvaluationMode.COMPONENT_FACTORY;
        var result = requiresComponentFactory(expr.getSourceLoc(), evaluate(expr));
        mode = EvaluationMode.COMMON;
        return result;
    }

    public Entity evaluate(ExprNode expr) {
        return switch (expr) {
            case AccessExpr sub -> evaluateAccessExpr(sub);
            case BinaryExpr sub -> evaluateBinaryExpr(sub);
            case ConditionalExpr sub -> evaluateConditionalExpr(sub);
            case ListExpr sub -> evaluateListExpr(sub);
            case NumberExpr sub -> evaluateNumberExpr(sub);
            case SymbolExpr sub -> evaluateSymbolExpr(sub);
            case UnaryExpr sub -> evaluateUnaryExpr(sub);
        };
    }

    private Entity evaluateAccessExpr(AccessExpr expr) {
        var base = evaluate(expr.getBase());
        var selector = evaluateSelector(expr.getSelector());
        return switch (base) {
            case Component component -> ComponentAccess.of(component, selector);
            case ListEntity listValue -> listValue.select(selector);
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
        var list = switch (expr) {
            case CommonList sub -> evaluateCommonList(sub);
            case RangeList sub -> evaluateRangeList(sub);
            case ListComprehension sub -> evaluateListComprehension(sub);
        };
        if (!list.entities().isEmpty()) {
            var type = list.entities().getFirst().getClass();
            if (!list.entities().stream().allMatch(type::isInstance)) {
                log.error("{}: list containing different types of elements: {}", expr.getSourceLoc(), list);
                throw new CompileCancellationException();
            }
        }
        return list;
    }

    private ListEntity evaluateCommonList(CommonList list) {
        var elements = list.getElements().stream().map(this::evaluate).toList();
        if (elements.isEmpty()) {
            return ListEntity.ofEmpty();
        }
        return ListEntity.of(elements);
    }

    private ListEntity evaluateRangeList(RangeList list) {
        var from = evaluateToIntegerValue(list.getFrom()).toInt();
        var to = evaluateToIntegerValue(list.getTo()).toInt();
        return ListEntity.of(
                IntStream.range(Integer.min(from, to), Integer.max(from, to))
                        .mapToObj(Int32Value::of)
                        .map(Entity.class::cast).toList());
    }

    private ListEntity evaluateListComprehension(ListComprehension list) {
        return ListEntity.of(doListComprehension(list.getHead(), new LinkedList<>(list.getQualifiers())));
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
                    overrideBindings(SingleBinding.of(
                            getIdentifierFrom(binding.getHead()),
                            evaluate(binding.getExpr())));
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
            case IntNumber intNumber -> Int32Value.of(intNumber.getValue());
        };
    }

    private Entity evaluateUnaryExpr(UnaryExpr expr) {
        return switch (expr.getOp()) {
            case ADD -> evaluateToIntegerValue(expr.getOperand());
            case SUB -> evaluateToIntegerValue(expr.getOperand()).neg();
            case NOT -> evaluateToBoolean(expr.getOperand()).not();
            case BITWISE_NOT -> evaluateToIntegerValue(expr.getOperand()).bitwiseNot();
        };
    }

    private static final Map<BinaryExpr.Operator, BiFunction<IntegerValue, IntegerValue, Value>> BINARY_OPERATOR_MAP = Map.ofEntries(
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

    private Entity evaluateBinaryExpr(BinaryExpr expr) {
        var sourceLoc = expr.getSourceLoc();
        return switch (expr.getOp()) {
            case COMBINE -> evaluateCombineType(sourceLoc, evaluate(expr.getLhs()), evaluate(expr.getRhs()));
            case MAPSTO -> evaluatePort(sourceLoc, evaluate(expr.getLhs()), evaluate(expr.getRhs()));
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

    private Entity evaluateCombineType(SourceLoc sourceLoc, Entity lhs, Entity rhs) {
        if (lhs instanceof ListEntity lhsList) {
            lhs = makeParallelTypeFromList(sourceLoc, lhsList);
        }
        if (rhs instanceof ListEntity rhsList) {
            rhs = makeParallelTypeFromList(sourceLoc, rhsList);
        }
        if (!(lhs instanceof InstanceType lhsType && rhs instanceof InstanceType rhsType)) {
            log.error("Expected instance type but got {} and {}", lhs, rhs);
            throw new CompileCancellationException();
        }
        return CombineType.from(lhsType, rhsType);
    }

    private ParallelType makeParallelTypeFromList(SourceLoc sourceLoc, ListEntity list) {
        if (list.entities().stream().allMatch(Predicate.not(InstanceType.class::isInstance))) {
            log.error("{}: Expected list of instance types but got {}", sourceLoc, list);
            throw new CompileCancellationException();
        }
        return ParallelType.from(list.entities().stream().map(InstanceType.class::cast).toList());
    }

    private Port evaluatePort(SourceLoc sourceLoc, Entity lhs, Entity rhs) {
        if (!(lhs instanceof ListEntity lhsList && rhs instanceof ListEntity rhsList)) {
            log.error("{}: Expected list of components but got {} and {}", sourceLoc, lhs, rhs);
            throw new CompileCancellationException();
        }
        return Port.of(makeAtomPortFromList(sourceLoc, lhsList), makeAtomPortFromList(sourceLoc, rhsList));
    }

    private List<Component> makeAtomPortFromList(SourceLoc sourceLoc, ListEntity list) {
        if (!list.entities().isEmpty()
                && list.entities().stream().allMatch(Predicate.not(Component.class::isInstance))) {
            log.error("{}: Expected list of components but got {}", sourceLoc, list);
            throw new CompileCancellationException();
        }
        return list.entities().stream().map(Component.class::cast).toList();
    }

    private Wire.Factory evaluateWireFactory(SourceLoc sourceLoc, List<ExprNode> arguments) {
        if (arguments.size() == 1) {
            return Wire.Factory.of(scope, evaluateBitWidth(arguments.getFirst()));
        }
        log.error("{}: Unexpected specification for component type wire: {}", sourceLoc, arguments);
        throw new CompileCancellationException();
    }

    private Clock.Factory evaluateClockFactory(SourceLoc sourceLoc, List<ExprNode> arguments) {
        if (arguments.isEmpty()) {
            return Clock.Factory.of(scope);
        } else {
            log.error("{}: Unexpected specification for component type clock: {}", sourceLoc, arguments);
            throw new CompileCancellationException();
        }
    }

    private int evaluateBitWidth(ExprNode expr) {
        if (expr instanceof ParamSymbolExpr spec) {
            var identifier = getIdentifierFrom(spec.getHead());
            if (identifier.string().equals("bit")) {
                if (spec.getArguments().size() == 1) {
                    return evaluateToIntegerValue(spec.getArguments().getFirst()).toInt();
                }
            }
        }
        log.error("{}: Unexpected bit width specification: {}", expr.getSourceLoc(), expr);
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
        for (var binding : bindings) {
            var entity = binding.lookup(id);
            if (entity != null) {
                return entity;
            }
        }
        if (scope instanceof DesignScope design) {
            if (design.containsInfo(id)) {
                return AtomType.of(id);
            }
            log.error("{}: Unresolved Symbol {}", expr.getSourceLoc(), id);
            throw new CompileCancellationException();
        }
        return scope.getParent().getEvaluator().evaluate(expr);
    }

    private Entity evaluateParamSymbolExpr(ParamSymbolExpr expr) {
        return switch (mode) {
            case COMPONENT_FACTORY -> evaluateComponentFactory(expr);
            case COMMON -> evaluateInstanceType(expr);
        };
    }

    private Component.Factory<?> evaluateComponentFactory(ParamSymbolExpr expr) {
        var identifier = getIdentifierFrom(expr.getHead());
        var sourceLoc = expr.getSourceLoc();
        return switch (identifier.string()) {
            case "wire" -> evaluateWireFactory(sourceLoc, expr.getArguments());
            case "clock" -> evaluateClockFactory(sourceLoc, expr.getArguments());
            default -> {
                log.error("{}: Unknown component type {}", expr.getSourceLoc(), expr);
                throw new CompileCancellationException();
            }
        };
    }

    private InstanceType evaluateInstanceType(ParamSymbolExpr expr) {
        var identifier = getIdentifierFrom(expr.getHead());
        var parameters = expr.getArguments().stream().map(this::evaluate).toList();
        return AtomType.of(identifier, parameters);
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
        var result = Identifier.of(sb.toString());
        result.setSourceLoc(interpolated.getSourceLoc());
        return result;
    }

    private void overrideBindings(pascal.hardscript.compiler.model.scope.Binding binding) {
        bindings.addFirst(binding);
    }

    private void restoreBindings() {
        bindings.removeFirst();
    }

    enum EvaluationMode {
        COMMON, COMPONENT_FACTORY
    }
}
