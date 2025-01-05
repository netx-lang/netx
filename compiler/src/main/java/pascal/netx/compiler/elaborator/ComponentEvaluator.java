package pascal.netx.compiler.elaborator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pascal.netx.compiler.ast.element.BindingNode;
import pascal.netx.compiler.ast.expr.*;
import pascal.netx.compiler.ast.expr.qualifier.BindingQualifier;
import pascal.netx.compiler.ast.expr.qualifier.FilterQualifier;
import pascal.netx.compiler.ast.expr.qualifier.GeneratorQualifier;
import pascal.netx.compiler.ast.expr.qualifier.Qualifier;
import pascal.netx.compiler.exception.CompileCancellationException;
import pascal.netx.compiler.model.Entity;
import pascal.netx.compiler.model.EntityList;
import pascal.netx.compiler.model.Utils;
import pascal.netx.compiler.model.component.*;
import pascal.netx.compiler.model.instance.*;
import pascal.netx.compiler.model.instance.selector.BitSelector;
import pascal.netx.compiler.model.instance.selector.PartSelector;
import pascal.netx.compiler.model.instance.selector.Selector;
import pascal.netx.compiler.model.instance.type.BitType;
import pascal.netx.compiler.model.instance.type.PolyType;
import pascal.netx.compiler.model.scope.ComponentScope;
import pascal.netx.compiler.model.scope.ModuleScope;
import pascal.netx.compiler.model.scope.binding.MonoBinding;
import pascal.netx.compiler.model.scope.binding.MultiBinding;
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

    private final ModuleScope module;
    private final ComponentScope scope;

    private EvaluationMode mode = EvaluationMode.COMMON;

    public ComponentEvaluator(ComponentScope component) {
        this.scope = component;
        this.module = component.getModule();
    }

    public Expr evaluateToComponentType(ExprNode expr, boolean needDeclareInPlace) {
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

    public int evaluateToInt(ExprNode expr) {
        return evaluateToIntegerValue(expr).toInt();
    }

    public BooleanValue evaluateToBoolean(ExprNode expr) {
        return castToBooleanValue(expr.getSourceLoc(), evaluate(expr));
    }

    public DataBundle evaluateDataBundle(ExprNode expr) {
        return requiresDataBundle(expr.getSourceLoc(), evaluate(expr));
    }

    public MonoBinding evaluateBinding(BindingNode binding) {
        Identifier id;
        List<Identifier> freeVariables;
        switch (binding.getHead()) {
            case ParamSymbolExpr param -> {
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
            case ConditionalExpr sub -> evaluateConditionalExpr(sub);
        };
    }

    private Entity evaluateFieldExpr(FieldExpr expr) {
        var bundle = evaluateDataBundle(expr.getBase());
        return bundle.getField(expr.getField());
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
                    var id = generator.getHead();
                    var list = evaluateToList(generator.getFrom());
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
            case SINK -> evaluateDecoratedInstanceAtom(InstanceAtom.Mode.SINK, expr);
            case SOURCE -> evaluateDecoratedInstanceAtom(InstanceAtom.Mode.SOURCE, expr);
        };
    }

    private Entity evaluateDecoratedInstanceAtom(InstanceAtom.Mode mode, UnaryExpr expr) {
        var operand = evaluate(expr.getOperand());
        switch (operand) {
            case EntityList(var list) -> {
                List<Expr> result = new LinkedList<>();
                for (var element : list) {
                    var inst = requiresMutableInstance(expr.getSourceLoc(), element);
                    result.add(InstanceAtom.of(mode, inst, expr.getSourceLoc()));
                }
                return ParallelExpr.from(result);
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
            factory = new Wire.Factory(PolyType.getInstance(), module);
        } else {
            mode = EvaluationMode.FACTORY;
            factory = requiresInstanceFactory(expr.getSourceLoc(), evaluate(expr));
            mode = EvaluationMode.COMMON;
        }
        return factory;
    }

    private Entity evaluateBinaryExpr(BinaryExpr expr) {
        return switch (expr.getOp()) {
            case COMBINE -> evaluateSequentialExpr(expr, false);
            case TIE -> evaluateSequentialExpr(expr, true);
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

    private Entity evaluateSequentialExpr(BinaryExpr expr, boolean isTied) {
        var leftLoc = expr.getLhs().getSourceLoc();
        var rightLoc = expr.getRhs().getSourceLoc();
        Expr lhs, rhs = castToComponentType(evaluate(expr.getRhs()), rightLoc);
        if (isTied) {
            var lhsEntity = evaluate(expr.getLhs());
            if (lhsEntity instanceof EntityList(var list)) {
                return ParallelExpr.from(list.stream()
                        .map(element -> requiresDataBundle(leftLoc, element))
                        .map(Expr.class::cast).toList());
            } else {
                lhs = InstanceAtom.of(InstanceAtom.Mode.BIDI,
                        requiresMutableInstance(leftLoc, lhsEntity), leftLoc);
            }
            return SequentialExpr.ofTied(lhs, rhs);
        } else {
            lhs = castToComponentType(evaluate(expr.getLhs()), leftLoc);
        }
        return SequentialExpr.ofSplit(lhs, rhs);
    }

    private Entity evaluateSymbolExpr(SymbolExpr expr) {
        return switch (expr) {
            case IdentifierExpr sub -> evaluatePrimarySymbolExpr(sub);
            case ParamSymbolExpr sub -> evaluateParamSymbolExpr(sub);
        };
    }

    private Entity evaluatePrimarySymbolExpr(IdentifierExpr expr) {
        var id = expr.getIdentifier();
        if (mode == EvaluationMode.FACTORY) {
            if (!scope.getModule().hasInterface(id)) {
                log.error("{}: Unknown interface type {}", expr.getSourceLoc(), id);
                throw new CompileCancellationException();
            }
            return scope.getModule().getInterface(id);
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
        int size = Utils.requiresMatchedSize(sourceLoc, marco.identifiers(), parameters);
        var bindings = MultiBinding.ofEmpty();
        for (int i = 0; i < size; i++) {
            bindings.bind(marco.identifiers().get(i), parameters.get(i));
        }
        scope.overrideBindings(bindings);
        var result = evaluate(marco.expr());
        scope.restoreBindings();
        return result;
    }

    @Nonnull
    private Entity resolveIdentifier(Identifier id) {
        Entity entity = scope.lookup(id);
        if (entity == null) {
            entity = module.lookup(id);
            if (entity == null) {
                if (module.hasComponent(id)) {
                    entity = ComponentAtom.of(module.getComponent(id), id);
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
        var factory = new Wire.Factory(PolyType.getInstance(), module);
        var data = factory.create(scope, id);
        scope.bind(id, data);
        return data;
    }

    private Entity evaluateComponentType(ParamSymbolExpr expr) {
        Entity prototype = resolveIdentifier(expr.getHead());
        List<Entity> parameters = evaluateParameters(expr);
        if (prototype instanceof Macro macro) {
            return evaluateMacroExpr(expr.getSourceLoc(), macro, parameters);
        } else if (prototype instanceof AtomExpr atom) {
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
                case Expr type -> type;
                case Data ignored -> castToComponentType(entity, arg.getSourceLoc());
                case DataAccess ignored -> castToComponentType(entity, arg.getSourceLoc());
                default -> requiresValue(arg.getSourceLoc(), entity);
            };
            result.add(param);
        }
        return result;
    }

    private MutableInstance.Factory<?> evaluateInstanceFactory(ParamSymbolExpr expr) {
        var sourceLoc = expr.getSourceLoc();
        return switch (expr.getHead().name()) {
            case "bit" -> evaluateWireFactory(sourceLoc, expr.getArguments());
            case "clock" -> evaluateClockFactory(sourceLoc, expr.getArguments());
            default -> {
                log.error("{}: Unknown component type {}", expr.getSourceLoc(), expr);
                throw new CompileCancellationException();
            }
        };
    }

    private Wire.Factory evaluateWireFactory(SourceLoc sourceLoc, List<ExprNode> arguments) {
        Utils.requiresMatchedSize(sourceLoc, 1, arguments);
        mode = EvaluationMode.COMMON;
        var bitType = BitType.of(evaluateToInt(arguments.getFirst()));
        mode = EvaluationMode.FACTORY;
        return new Wire.Factory(bitType, module);
    }

    private Clock.Factory evaluateClockFactory(SourceLoc sourceLoc, List<ExprNode> arguments) {
        if (arguments.isEmpty()) {
            return new Clock.Factory(module);
        } else {
            log.error("{}: Unexpected specification for component type clock: {}", sourceLoc, arguments);
            throw new CompileCancellationException();
        }
    }

    enum EvaluationMode {
        COMMON, FACTORY, DECLARE_IN_PLACE
    }
}
