package pascal.netx.compiler.elaborator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pascal.netx.compiler.ast.element.BindingNode;
import pascal.netx.compiler.ast.element.InterfaceNode;
import pascal.netx.compiler.ast.expr.*;
import pascal.netx.compiler.ast.expr.qualifier.BindingQualifier;
import pascal.netx.compiler.ast.expr.qualifier.FilterQualifier;
import pascal.netx.compiler.ast.expr.qualifier.GeneratorQualifier;
import pascal.netx.compiler.ast.expr.qualifier.Qualifier;
import pascal.netx.compiler.exception.CompileCancellationException;
import pascal.netx.compiler.model.Entity;
import pascal.netx.compiler.model.EntityList;
import pascal.netx.compiler.model.Macro;
import pascal.netx.compiler.model.Utils;
import pascal.netx.compiler.model.expr.*;
import pascal.netx.compiler.model.scope.Attributes;
import pascal.netx.compiler.model.scope.ComponentScope;
import pascal.netx.compiler.model.scope.PackageScope;
import pascal.netx.compiler.model.scope.binding.ArgBindList;
import pascal.netx.compiler.model.scope.binding.MonoBinding;
import pascal.netx.compiler.model.scope.binding.MultiBinding;
import pascal.netx.compiler.model.scope.binding.OrderedArgBindList;
import pascal.netx.compiler.model.variable.*;
import pascal.netx.compiler.model.variable.selector.BitSelector;
import pascal.netx.compiler.model.variable.selector.PartSelector;
import pascal.netx.compiler.model.variable.selector.Selector;
import pascal.netx.compiler.model.variable.type.BitType;
import pascal.netx.compiler.model.variable.type.PolyType;
import pascal.netx.util.exception.ShallNotReachHereError;
import pascal.netx.util.lang.BuiltinComponent;
import pascal.netx.util.lang.Identifier;
import pascal.netx.util.lang.SourceLoc;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.stream.IntStream;

import static pascal.netx.compiler.model.Entities.*;

public final class ComponentEvaluator {
    private static final Logger log = LoggerFactory.getLogger(ComponentEvaluator.class);
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

    private final PackageScope pack;
    private final ComponentScope scope;

    private EvaluationMode mode = EvaluationMode.COMMON;

    public ComponentEvaluator(ComponentScope component) {
        this.scope = component;
        this.pack = component.getPackage();
    }

    private static boolean compareListedValue(Entity lhs, Entity rhs) {
        switch (lhs) {
            case EntityList(var lhsList) -> {
                if (rhs instanceof EntityList(var rhsList)) {
                    if (lhsList.size() == rhsList.size()) {
                        return IntStream.range(0, lhsList.size())
                                .allMatch(i -> compareListedValue(lhsList.get(i), rhsList.get(i)));
                    }
                }
            }
            case IntegerValue lhsInt -> {
                if (rhs instanceof IntegerValue rhsInt) {
                    return lhsInt.eq(rhsInt).value();
                }
            }
            case BooleanValue lhsBool -> {
                if (rhs instanceof BooleanValue rhsBool) {
                    return lhsBool.eq(rhsBool).value();
                }
            }
            default -> throw new ShallNotReachHereError();
        }
        return false;
    }

    private static Entity castArgument(SourceLoc loc, Entity entity) {
        return switch (entity) {
            case Expr type -> type;
            case Data ignored -> castToInstantiateExpr(loc, entity);
            case Access ignored -> castToInstantiateExpr(loc, entity);
            default -> entity;
        };
    }

    public Expr evaluateToComponentExpr(ExprNode expr, boolean needDeclareInPlace) {
        if (needDeclareInPlace) {
            return castToInstantiateExpr(expr.getSourceLoc(), evaluateAndDeclInPlace(expr));
        }
        return castToInstantiateExpr(expr.getSourceLoc(), evaluate(expr));
    }

    public EntityList castToList(ExprNode expr) {
        var entity = evaluate(expr);
        if (entity instanceof EntityList list) {
            return list;
        } else {
            return EntityList.of(List.of(entity));
        }
    }

    public IntegerValue evaluateToIntegerValue(ExprNode expr) {
        return requiresIntegerValue(expr.getSourceLoc(), evaluate(expr));
    }

    public int evaluateToInt(ExprNode expr) {
        return evaluateToIntegerValue(expr).toInt();
    }

    public BooleanValue evaluateToBoolean(ExprNode expr) {
        return castToBooleanValue(expr.getSourceLoc(), evaluate(expr));
    }

    public Bundle evaluateDataBundle(ExprNode expr) {
        return requiresDataBundle(expr.getSourceLoc(), evaluate(expr));
    }

    public MonoBinding evaluateBinding(BindingNode binding) {
        Identifier id;
        List<Identifier> freeVariables;
        switch (binding.getHead()) {
            case CallSymbolExpr param -> {
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
        return MonoBinding.of(id, Macro.of(freeVariables, binding.getExpr()));
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
            case PortConnectionExpr sub -> evaluatePortExpr(sub);
            case ConditionalExpr sub -> evaluateConditionalExpr(sub);
        };
    }

    private Entity evaluateFieldExpr(FieldExpr expr) {
        if (expr.getBase() instanceof IdentifierExpr idExpr) {
            var id = idExpr.getIdentifier();
            if (pack.defs().hasEnum(id)) {
                return pack.defs().getEnum(id).fields().get(expr.getField());
            }
        }
        var bundle = evaluateDataBundle(expr.getBase());
        return bundle.getField(expr.getField());
    }

    private Entity evaluateAccessExpr(AccessExpr expr) {
        var base = evaluate(expr.getBase());
        var selector = evaluateSelector(expr.getSelector());
        return switch (base) {
            case Variable inst -> {
                try {
                    yield Variable.accessOf(inst, selector);
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

    private Selector evaluateSelector(pascal.netx.compiler.ast.expr.selector.Selector selector) {
        return switch (selector) {
            case pascal.netx.compiler.ast.expr.selector.BitSelector bitSelector ->
                    BitSelector.of(evaluateToIntegerValue(bitSelector.getIndex()));
            case pascal.netx.compiler.ast.expr.selector.PartSelector partSelector -> PartSelector.of(
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
        var from = evaluateToInt(list.getFrom());
        var to = evaluateToInt(list.getTo());
        var result = new ArrayList<>(
                IntStream.range(Integer.min(from, to), Integer.max(from, to))
                        .mapToObj(Int32Value::of)
                        .map(Entity.class::cast)
                        .toList());
        if (from > to) {
            Collections.reverse(result);
        }
        return EntityList.of(result);
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
                    var id = generator.getHead();
                    var list = castToList(generator.getFrom());
                    for (var entity : list.entities()) {
                        scope.overrideBindings(MonoBinding.of(id, entity));
                        result.addAll(doListComprehension(head, new LinkedList<>(qualifiers)));
                        scope.restoreBindings();
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
                    scope.overrideBindings(evaluateBinding(binding));
                    result = doListComprehension(head, new LinkedList<>(qualifiers));
                    scope.restoreBindings();
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
            case SINK -> evaluateDecoratedExpr(DecoratedExpr.Decorator.SINK, expr.getOperand());
            case SOURCE -> evaluateDecoratedExpr(DecoratedExpr.Decorator.SOURCE, expr.getOperand());
        };
    }

    private Entity evaluateDecoratedExpr(DecoratedExpr.Decorator decorator, ExprNode base) {
        var operand = evaluate(base);
        return switch (operand) {
            case EntityList(var list) -> {
                List<Expr> result = new LinkedList<>();
                for (var element : list) {
                    if (!(element instanceof Expr expr)) {
                        log.error("{}: Expected instantiate expr but got {}", base.getSourceLoc(), element);
                        throw new CompileCancellationException();
                    }
                    result.add(DecoratedExpr.of(decorator, expr));
                }
                yield ParallelExpr.from(result);
            }
            case Expr expr -> DecoratedExpr.of(decorator, expr);
            case Variable variable -> DecoratedExpr.of(decorator, VariableRef.of(variable, base.getSourceLoc()));
            default -> {
                log.error("{}: Expected instantiate expr(s) but got {}", base.getSourceLoc(), operand);
                throw new CompileCancellationException();
            }
        };
    }

    public Mutable.Factory<?> evaluateToMutableFactory(ExprNode expr) {
        Mutable.Factory<?> factory;
        if (expr == null) {
            factory = new Wire.Factory(PolyType.getInstance(), Utils.compilerOf(pack));
        } else {
            mode = EvaluationMode.FACTORY;
            factory = requiresMutableFactory(expr.getSourceLoc(), evaluate(expr));
            mode = EvaluationMode.COMMON;
        }
        return factory;
    }

    private Entity evaluateBinaryExpr(BinaryExpr expr) {
        return switch (expr.getOp()) {
            case COMBINE -> evaluateSequentialExpr(expr);
            case CONCAT -> evaluateListConcat(expr);
            case EQ, NE -> evaluateEqualityExpr(expr);
            case ADD, SUB, MUL, DIV, MOD, POW, AND, OR, XOR, XNOR,
                 LT, LE, GT, GE, ARITH_SHR, ARITH_SHL, LOGIC_SHR, LOGIC_SHL ->
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

    private Value evaluateEqualityExpr(BinaryExpr expr) {
        var lhs = requiresListedValue(expr.getLhs().getSourceLoc(), evaluate(expr.getLhs()));
        var rhs = requiresListedValue(expr.getRhs().getSourceLoc(), evaluate(expr.getRhs()));
        boolean isEqual = compareListedValue(lhs, rhs);
        boolean needEqual = expr.getOp() == BinaryExpr.Operator.EQ;
        return BooleanValue.of(needEqual == isEqual);
    }

    private EntityList evaluateListConcat(BinaryExpr expr) {
        List<Entity> result = new LinkedList<>();
        var lhs = castToList(expr.getLhs());
        var rhs = castToList(expr.getRhs());
        result.addAll(lhs.entities());
        result.addAll(rhs.entities());
        return EntityList.of(result);
    }

    private Entity evaluatePortExpr(PortConnectionExpr expr) {
        List<PortExpr.PortAssign> ports = new LinkedList<>();
        for (var assign : expr.getPorts()) {
            ports.add(PortExpr.PortAssign.of(
                    assign.lhs(), castToInstantiateExpr(assign.rhs().getSourceLoc(), evaluate(assign.rhs()))));
        }
        return PortExpr.of(expr.getSourceLoc(), ports);
    }

    private Entity evaluateSequentialExpr(BinaryExpr expr) {
        var leftLoc = expr.getLhs().getSourceLoc();
        var rightLoc = expr.getRhs().getSourceLoc();
        var rhs = castToInstantiateExpr(rightLoc, evaluate(expr.getRhs()));
        var lhs = castToInstantiateExpr(leftLoc, evaluate(expr.getLhs()));
        return SequentialExpr.of(lhs, rhs);
    }

    private Entity evaluateSymbolExpr(SymbolExpr expr) {
        return switch (expr) {
            case IdentifierExpr sub -> evaluatePrimarySymbolExpr(sub);
            case CallSymbolExpr sub -> evaluateParamSymbolExpr(sub);
        };
    }

    private Entity evaluatePrimarySymbolExpr(IdentifierExpr expr) {
        var id = expr.getIdentifier();
        if (mode == EvaluationMode.FACTORY) {
            var pack = scope.getPackage();
            if (pack.defs().hasInterface(id)) {
                return evaluateInterfacrFactory(expr.getSourceLoc(),
                        pack.defs().getInterface(id), ArgBindList.ofEmpty());
            } else if (pack.defs().hasEnum(id)) {
                return new Wire.Factory(BitType.of(pack.defs().getEnum(id).width()), Utils.compilerOf(pack));
            } else {
                log.error("{}: Unknown interface type {}", expr.getSourceLoc(), id);
                throw new CompileCancellationException();
            }
        } else {
            Entity entity = resolveIdentifier(id);
            if (entity instanceof Macro macro) {
                return evaluateMacroExpr(expr.getSourceLoc(), macro, ArgBindList.ofEmpty());
            }
            return entity;
        }
    }

    private Entity evaluateParamSymbolExpr(CallSymbolExpr expr) {
        if (mode == EvaluationMode.FACTORY) {
            return evaluateVariableFactory(expr);
        } else {
            return evaluateInstantiateExpr(expr);
        }
    }

    public Entity evaluateMacroExpr(SourceLoc sourceLoc, Macro marco, ArgBindList parameters) {
        var override = switch (parameters) {
            case MultiBinding binding -> binding.copyOf();
            case OrderedArgBindList(var args) -> {
                int size = Utils.requiresMatchedSize(sourceLoc, marco.identifiers(), args);
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

    @Nonnull
    private Entity resolveIdentifier(Identifier id) {
        Entity entity = scope.lookup(id);
        if (entity == null) {
            entity = pack.lookup(id);
            if (entity == null) {
                if (pack.defs().hasComponent(id)) {
                    entity = ComponentRef.of(pack.defs().getComponent(id), id);
                } else if (pack.defs().hasImport(id)) {
                    entity = pack.defs().getImport(id);
                } else if (BuiltinComponent.isBuiltin(id)) {
                    entity = BuiltinRef.of(BuiltinComponent.of(id), id.getSourceLoc());
                } else {
                    entity = pack.defs().lookupEnum(id);
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
        var factory = new Wire.Factory(PolyType.getInstance(), Utils.compilerOf(pack));
        var data = factory.create(scope, id);
        scope.bind(id, data);
        return data;
    }

    private Entity evaluateInstantiateExpr(CallSymbolExpr expr) {
        Entity prototype = resolveIdentifier(expr.getHead());
        ArgBindList arguments = evaluateArguments(expr);
        if (prototype instanceof Macro macro) {
            return evaluateMacroExpr(expr.getSourceLoc(), macro, arguments);
        } else if (prototype instanceof PrimaryExpr atom) {
            atom.setArgList(arguments);
            return atom;
        } else {
            log.error("{}: Expected component type but got {}", expr.getSourceLoc(), prototype);
            throw new CompileCancellationException();
        }
    }

    private ArgBindList evaluateArguments(CallSymbolExpr expr) {
        if (expr.getAssigns().isEmpty() || expr.getAssigns().getFirst().first() == null) {
            var result = OrderedArgBindList.ofEmpty();
            for (var argument : expr.getArguments()) {
                result.add(castArgument(argument.getSourceLoc(), evaluate(argument)));
            }
            return result;
        } else {
            var result = MultiBinding.ofEmpty();
            for (var assign : expr.getAssigns()) {
                var argument = castArgument(assign.second().getSourceLoc(), evaluate(assign.second()));
                result.bind(assign.first(), argument);
            }
            return result;
        }
    }

    private Mutable.Factory<?> evaluateVariableFactory(CallSymbolExpr expr) {
        var sourceLoc = expr.getSourceLoc();
        return switch (expr.getHead().name()) {
            case "bit" -> evaluateWireFactory(sourceLoc, expr.getArguments());
            case "clock" -> evaluateClockFactory(sourceLoc, expr.getArguments());
            default -> {
                if (pack.defs().hasInterface(expr.getHead())) {
                    var def = pack.defs().getInterface(expr.getHead());
                    yield evaluateInterfacrFactory(expr.getSourceLoc(), def, evaluateArguments(expr));
                }
                log.error("{}: Unknown component type {}", expr.getSourceLoc(), expr);
                throw new CompileCancellationException();
            }
        };
    }

    private Bundle.Factory evaluateInterfacrFactory(SourceLoc sourceLoc, InterfaceNode node, ArgBindList arguments) {
        var component = ComponentScope.newInterface(scope, node.getIdentifier(), false);
        var attributes = Attributes.from(sourceLoc, node, arguments);
        for (var bind : attributes.entrySet()) {
            component.bind(bind.getKey(), bind.getValue());
        }
        return new InterfaceFactoryMaker(component).make(node);
    }

    private Wire.Factory evaluateWireFactory(SourceLoc sourceLoc, List<ExprNode> arguments) {
        Utils.requiresMatchedSize(sourceLoc, 1, arguments);
        mode = EvaluationMode.COMMON;
        var bitType = BitType.of(evaluateToInt(arguments.getFirst()));
        mode = EvaluationMode.FACTORY;
        return new Wire.Factory(bitType, Utils.compilerOf(pack));
    }

    private Clock.Factory evaluateClockFactory(SourceLoc sourceLoc, List<ExprNode> arguments) {
        if (arguments.isEmpty()) {
            return new Clock.Factory(Utils.compilerOf(pack));
        } else {
            log.error("{}: Unexpected specification for component type clock: {}", sourceLoc, arguments);
            throw new CompileCancellationException();
        }
    }

    enum EvaluationMode {
        COMMON, FACTORY, DECLARE_IN_PLACE
    }
}
