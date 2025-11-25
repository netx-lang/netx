package pascal.netx.compiler;

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
import pascal.netx.compiler.circuit.scope.Scope;
import pascal.netx.compiler.circuit.scope.binding.AttrBindList;
import pascal.netx.compiler.circuit.scope.binding.MonoBinding;
import pascal.netx.compiler.circuit.scope.binding.MultiBinding;
import pascal.netx.compiler.circuit.scope.binding.OrderedAttrBindList;
import pascal.netx.compiler.circuit.variable.*;
import pascal.netx.compiler.circuit.variable.selector.BitSelector;
import pascal.netx.compiler.circuit.variable.selector.PartSelector;
import pascal.netx.compiler.circuit.variable.selector.Selector;
import pascal.netx.compiler.circuit.variable.type.BitType;
import pascal.netx.compiler.circuit.variable.type.PolyType;
import pascal.netx.compiler.circuit.variable.type.Type;
import pascal.netx.compiler.io.Configuration;
import pascal.netx.compiler.utils.CompileCancellationException;
import pascal.netx.compiler.utils.Logger;
import pascal.netx.util.lang.BuiltinComponent;
import pascal.netx.util.lang.Identifier;
import pascal.netx.util.lang.SourceLoc;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.IntStream;

import static pascal.netx.compiler.circuit.Entities.*;
import static pascal.netx.compiler.circuit.Utils.compilerOf;
import static pascal.netx.compiler.circuit.Utils.repoOf;
import static pascal.netx.compiler.circuit.scope.Attributes.from;
import static pascal.netx.util.lang.BuiltinComponent.REG_CLK_EDGE_ATTR;
import static pascal.netx.util.lang.BuiltinComponent.TO;

public final class Evaluator {
    private final PackageScope pack;
    private final ComponentScope scope;
    private final BuiltInComponentFactory factory;

    private EvaluationMode mode = EvaluationMode.COMMON;

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
            Map.entry(BinaryExpr.Operator.SLT, IntegerValue::slt),
            Map.entry(BinaryExpr.Operator.SLE, IntegerValue::sle),
            Map.entry(BinaryExpr.Operator.SGT, IntegerValue::sgt),
            Map.entry(BinaryExpr.Operator.SGE, IntegerValue::sge),
            Map.entry(BinaryExpr.Operator.ULT, IntegerValue::ult),
            Map.entry(BinaryExpr.Operator.ULE, IntegerValue::ule),
            Map.entry(BinaryExpr.Operator.UGT, IntegerValue::ugt),
            Map.entry(BinaryExpr.Operator.UGE, IntegerValue::uge)
    );

    public Evaluator(ComponentScope component) {
        this.scope = component;
        this.pack = component.getPackage();
        this.factory = new BuiltInComponentFactory(compilerOf(scope));
    }

    public EntityList castToList(ExprNode expr) {
        var entity = evaluate(expr);
        if (entity instanceof EntityList list) {
            return list;
        } else {
            return EntityList.of(List.of(entity));
        }
    }

    private static Entity castArgument(Entity entity) {
        return switch (entity) {
            case Expr type -> type;
            case Data ignored -> castToInstantiateExpr(entity);
            case Access ignored -> castToInstantiateExpr(entity);
            default -> entity;
        };
    }

    public int evaluateToInt(ExprNode expr) {
        return evaluateToIntegerValue(expr).toInt();
    }

    public IntegerValue evaluateToIntegerValue(ExprNode expr) {
        return requiresIntegerValue(evaluate(expr));
    }

    public BooleanValue evaluateToBoolean(ExprNode expr) {
        return castToBooleanValue(evaluate(expr));
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
        var macro = Macro.of(freeVariables, binding.getExpr());
        checkClosedMacro(binding.getSourceLoc(), id, macro);
        return MonoBinding.of(id, macro);
    }

    private void checkClosedMacro(SourceLoc sourceLoc, Identifier name, Macro macro) {
        var freeVariables = FreeVariableDetector.detect(macro.expr());
        macro.identifiers().forEach(freeVariables::remove);
        for (var id : freeVariables) {
            if (!name.equals(id)) { // Not a recursive macro
                try {
                    resolveIdentifier(id);
                } catch (CompileCancellationException e) {
                    Logger.error(sourceLoc, "Macro {} is not closed, missing variable {}", macro.expr(), id);
                    throw e;
                }
            }
        }
    }

    public Entity evaluateAndDeclInPlace(ExprNode expr) {
        mode = EvaluationMode.DECLARE_IN_PLACE;
        var entity = evaluate(expr);
        mode = EvaluationMode.COMMON;
        return entity;
    }

    public Entity evaluate(ExprNode expr) {
        return switch (expr) {
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
                    Logger.error(expr.getSourceLoc(), "invalid selectors {}", expr);
                    throw e;
                }
            }
            case VariableRef ref -> {
                try {
                    yield Variable.accessOf(ref.getInstance(), selector);
                } catch (CompileCancellationException e) {
                    Logger.error(expr.getSourceLoc(), "invalid selectors {}", expr);
                    throw e;
                }
            }
            case EntityList list -> list.select(selector);
            default -> {
                Logger.error(expr.getBase().getSourceLoc(), "Expected variable or list but got {}",
                        base);
                throw new CompileCancellationException();
            }
        };
    }

    private Selector evaluateSelector(pascal.netx.compiler.ast.expr.selector.Selector selector) {
        return switch (selector) {
            case pascal.netx.compiler.ast.expr.selector.BitSelector bitSelector ->
                    new BitSelector(evaluateToIntegerValue(bitSelector.getIndex()));
            case pascal.netx.compiler.ast.expr.selector.PartSelector partSelector -> new PartSelector(
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

    private Entity evaluateLetExpr(LetExpr expr) {
        var binding = evaluateBinding(expr.getBinding());
        scope.overrideBindings(binding);
        var result = evaluate(expr.getBody());
        scope.restoreBindings();
        return result;
    }

    private Entity evaluateNumberExpr(NumberExpr expr) {
        return switch (expr) {
            case BitVectorNumber bitVectorNumber -> BitVectorValue.of(bitVectorNumber.getRepresentation());
            case BoolNumber boolNumber -> BooleanValue.of(boolNumber.getValue());
            case IntNumber intNumber -> Int32Value.of(intNumber.getValue());
        };
    }

    private Entity evaluateStringExpr(StringExpr expr) {
        return StringLiteral.of(expr.getSourceLoc(), expr.getValue());
    }

    private Entity evaluateUnaryExpr(UnaryExpr expr) {
        return switch (expr.getOp()) {
            case ADD -> evaluateToIntegerValue(expr.getOperand());
            case SUB -> evaluateToIntegerValue(expr.getOperand()).neg();
            case NOT -> evaluateToBoolean(expr.getOperand()).not();
            case BITWISE_NOT -> evaluateToIntegerValue(expr.getOperand()).bitwiseNot();
            case BLOCK_OUT -> evaluateDecoratedExpr(DecoratedExpr.Decorator.BLOCK_OUT, expr.getOperand());
            case BLOCK_IN -> evaluateDecoratedExpr(DecoratedExpr.Decorator.BLOCK_IN, expr.getOperand());
        };
    }

    public Bundle evaluateDataBundle(ExprNode expr) {
        return requiresDataBundle(evaluate(expr));
    }

    private Entity evaluateDecoratedExpr(DecoratedExpr.Decorator decorator, ExprNode base) {
        var operand = evaluate(base);
        return switch (operand) {
            case EntityList(var list) -> {
                List<Expr> result = new LinkedList<>();
                for (var element : list) {
                    if (!(element instanceof Expr expr)) {
                        Logger.error(base.getSourceLoc(), "Expected instantiate expr but got {}", element);
                        throw new CompileCancellationException();
                    }
                    result.add(DecoratedExpr.of(decorator, expr));
                }
                yield ParallelExpr.from(result);
            }
            case Expr expr -> DecoratedExpr.of(decorator, expr);
            case Variable variable -> DecoratedExpr.of(decorator, VariableRef.of(variable));
            default -> {
                Logger.error(base.getSourceLoc(), "Expected instantiate expr(s) but got {}", operand);
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
            factory = requiresMutableFactory(evaluate(expr));
            mode = EvaluationMode.COMMON;
        }
        return factory;
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

    public void evaluate(ExprNode expr, boolean needDeclareInPlace) {
        Expr ee;
        if (needDeclareInPlace) {
            ee = castToInstantiateExpr(evaluateAndDeclInPlace(expr));
        } else {
            ee = castToInstantiateExpr(evaluate(expr));
        }
        new Connector().instantiate(ee);
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
        var ports = new PortExpr();
        for (var assign : expr.getPorts()) {
            var rhs = castToInstantiateExpr(evaluate(assign.rhs()));
            if (assign.isDefault()) {
                ports.setDefaultExpr(rhs);
            } else {
                ports.addPortAssign(assign.lhs(), rhs);
            }
        }
        return ports;
    }

    private Entity evaluateSequentialExpr(BinaryExpr expr) {
        var rhs = castToInstantiateExpr(evaluate(expr.getRhs()));
        var lhs = castToInstantiateExpr(evaluate(expr.getLhs()));
        return SequentialExpr.of(lhs, rhs);
    }

    private Entity evaluateSymbolExpr(SymbolExpr expr) {
        return switch (expr) {
            case IdentifierExpr sub -> evaluatePrimarySymbolExpr(sub);
            case CallSymbolExpr sub -> evaluateParamSymbolExpr(sub);
        };
    }

    private Value evaluateEqualityExpr(BinaryExpr expr) {
        var lhs = requiresListedValue(evaluate(expr.getLhs()));
        var rhs = requiresListedValue(evaluate(expr.getRhs()));
        boolean isEqual = Utils.isEqualListedValue(lhs, rhs);
        boolean needEqual = expr.getOp() == BinaryExpr.Operator.EQ;
        return BooleanValue.of(needEqual == isEqual);
    }

    private Entity evaluateParamSymbolExpr(CallSymbolExpr expr) {
        if (mode == EvaluationMode.FACTORY) {
            return evaluateVariableFactory(expr);
        } else {
            return evaluateCallExpr(expr);
        }
    }

    public Entity evaluateMacroExpr(SourceLoc sourceLoc, Macro marco, AttrBindList parameters) {
        var override = switch (parameters) {
            case MultiBinding binding -> binding.copyOf();
            case OrderedAttrBindList(var args) -> {
                // FIXME: message
                int size = Utils.requireEqSize(marco.identifiers(), args,
                        "Macro invocation requires all positional arguments");
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

    public Entity evaluateSystemMetaExpr(SourceLoc sourceLoc, SystemMeta meta, AttrBindList parameters) {
        return meta.apply(sourceLoc, parameters);
    }

    @Nonnull
    private Entity resolveIdentifier(Identifier id) {
        if (id.isSystemMeta()) {
            return SystemMeta.resolve(id);
        }

        Entity entity = scope.lookup(id);
        if (entity == null) {
            if (BuiltinComponent.isBuiltin(id)) {
                entity = BuiltinRef.of(BuiltinComponent.of(id), id.getSourceLoc());
            } else {
                entity = resolveIdentifierInPack(id);
            }
        }

        return Objects.requireNonNullElseGet(entity, () -> {
            if (mode == EvaluationMode.DECLARE_IN_PLACE) {
                return declareDataInPlace(id);
            }
            Logger.error(id.getSourceLoc(), "Unresolved identifier {}", id);
            throw new CompileCancellationException();
        });
    }

    private Entity resolveIdentifierInPack(Identifier id) {
        Entity entity = pack.lookup(id);
        if (entity == null) {
            if (pack.defs().hasComponent(id)) {
                entity = ComponentRef.of(pack.defs().getComponent(id), id);
            } else if (pack.defs().hasImport(id)) {
                entity = pack.defs().getImport(id);
            } else {
                entity = pack.defs().lookupEnum(id);
            }
        }

        // Handling cross-package resolution
        if (entity == null && scope.getParent() instanceof ComponentScope parent) {
            entity = parent.getEvaluator().resolveIdentifierInPack(id);
        }
        return entity;
    }

    @Nonnull
    private Entity declareDataInPlace(Identifier id) {
        var factory = new Wire.Factory(PolyType.getInstance(), Utils.compilerOf(pack));
        var data = factory.create(scope, id);
        scope.bind(id, data);
        return data;
    }

    private Entity evaluateCallExpr(CallSymbolExpr expr) {
        Entity prototype = resolveIdentifier(expr.getHead());
        AttrBindList arguments = evaluateArguments(expr);
        return switch (prototype) {
            case Macro macro -> evaluateMacroExpr(expr.getSourceLoc(), macro, arguments);
            case SystemMeta systemMeta -> evaluateSystemMetaExpr(expr.getSourceLoc(), systemMeta, arguments);
            case PrimaryExpr atom -> {
                atom.setArgList(arguments);
                yield atom;
            }
            default -> {
                Logger.error(expr.getSourceLoc(), "Expected component type but got {}", prototype);
                throw new CompileCancellationException();
            }
        };
    }

    private AttrBindList evaluateArguments(CallSymbolExpr expr) {
        if (expr.getAssigns().isEmpty() || expr.getAssigns().getFirst().first() == null) {
            var result = OrderedAttrBindList.ofEmpty();
            for (var argument : expr.getArguments()) {
                result.add(castArgument(evaluate(argument)));
            }
            return result;
        } else {
            var result = MultiBinding.ofEmpty();
            for (var assign : expr.getAssigns()) {
                var argument = castArgument(evaluate(assign.second()));
                result.bind(assign.first(), argument);
            }
            return result;
        }
    }

    private Entity evaluatePrimarySymbolExpr(IdentifierExpr expr) {
        var id = expr.getIdentifier();
        if (mode == EvaluationMode.FACTORY) {
            if (id.name().equals("clock")) {
                return evaluateClockFactory(expr.getSourceLoc(), List.of());
            }

            var pack = scope.getPackage();
            if (pack.defs().hasInterface(id)) {
                return evaluateInterfaceFactory(
                        pack.defs().getInterface(id), AttrBindList.ofEmpty());
            } else if (pack.defs().hasEnum(id)) {
                return new Wire.Factory(BitType.of(pack.defs().getEnum(id).width()), Utils.compilerOf(pack));
            } else {
                Logger.error(expr.getSourceLoc(), "Unknown interface type {}", id);
                throw new CompileCancellationException();
            }
        } else {
            Entity entity = resolveIdentifier(id);
            if (entity instanceof Macro macro) {
                return evaluateMacroExpr(expr.getSourceLoc(), macro, AttrBindList.ofEmpty());
            } else if (entity instanceof SystemMeta meta) {
                return evaluateSystemMetaExpr(expr.getSourceLoc(), meta, AttrBindList.ofEmpty());
            }
            return entity;
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
                    yield evaluateInterfaceFactory(def, evaluateArguments(expr));
                }
                Logger.error(expr.getSourceLoc(), "Unknown component type {}", expr);
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

    private Wire.Factory evaluateWireFactory(SourceLoc sourceLoc, List<ExprNode> arguments) {
        Utils.requireEqSize(1, arguments, "Wire factory requires one argument");
        mode = EvaluationMode.COMMON;
        var bitType = BitType.of(evaluateToInt(arguments.getFirst()));
        mode = EvaluationMode.FACTORY;
        return new Wire.Factory(bitType, Utils.compilerOf(pack));
    }

    private Clock.Factory evaluateClockFactory(SourceLoc sourceLoc, List<ExprNode> arguments) {
        if (arguments.isEmpty()) {
            return new Clock.Factory(Utils.compilerOf(pack));
        } else {
            Logger.error(sourceLoc, "Unexpected specification for component type clock: {}", arguments);
            throw new CompileCancellationException();
        }
    }

    enum EvaluationMode {
        COMMON, FACTORY, DECLARE_IN_PLACE
    }

    static final class InterfaceFactoryCreator {
        private final ComponentScope scope;
        private final NXCompiler compiler;

        public InterfaceFactoryCreator(ComponentScope scope) {
            this.scope = scope;
            this.compiler = Utils.compilerOf(scope);
        }

        public Bundle.Factory create(InterfaceNode node) {
            var config = Configuration.builder().build();
            var elaborator = new Elaborator.ComponentElaborator(scope);
            node.getElements().forEach(elaborator::buildElement);
            var fields = elaborator.extractIOList(node.getSig());
            LinkedHashMap<Identifier, Mutable.Factory<?>> factories = new LinkedHashMap<>();
            for (var inst : fields) {
                if (!(inst instanceof Mutable mutable)) {
                    Logger.error(node.getSourceLoc(), "Expected mutable instance but got {}", inst);
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
                        Logger.error(loc, "Interface shall not contain auto-typed instance: {}", wire);
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
                    Logger.error(loc, "Interface shall not contain data access: {}", mutable);
                    throw new CompileCancellationException();
                }
            };
        }
    }

    static final class BuiltInComponentFactory {
        private final NXCompiler compiler;

        public BuiltInComponentFactory(NXCompiler compiler) {
            this.compiler = compiler;
        }

        public static BuiltInComponentFactory create(NXCompiler compiler) {
            return new BuiltInComponentFactory(compiler);
        }

        private static void moveAttribute(Attributes attr, BuiltinRef ref, String name, int index,
                                          Function<Entity, ? extends  Entity> checker) {
            attr.addAttribute(Identifier.of(name), checker.apply(ref.getArgList().get(name, index)));
        }

        public ComponentScope createBuiltInComponent(Scope parent, BuiltinRef ref, int argNumHint) {
            if (BuiltinComponent.MULTI_INPUT.contains(ref.getBuiltin())) {
                return createMultiInputComponent(parent, ref, argNumHint);
            } else {
                return createBuiltInComponent(parent, ref);
            }
        }

        private ComponentScope createNotComponent(Scope parent, BuiltinRef ref) {
            var result = instantiate(parent, ref);
            result.addInput(newWire(result, "i"));
            result.addOutput(newWire(result, "o"));
            return result;
        }

        private ComponentScope createIdentityComponent(Scope parent, BuiltinRef ref) {
            var result = instantiate(parent, ref);
            var entity = newWire(result, "TO_DUMMY");
            result.addInput(entity);
            result.addOutput(entity);
            return result;
        }

        public ComponentScope createBuiltInComponent(Scope parent, BuiltinRef ref) {
            return switch (ref.getBuiltin()) {
                case REGISTER -> createRegisterComponent(parent, ref);
                case ASYNC_REGISTER -> createAsyncRegisterComponent(parent, ref);
                case DUP -> createDupComponent(parent, ref);
                case SPLIT -> createSplitComponent(parent, ref);
                case TO -> createIdentityComponent(parent, ref);
                case UCAST, CAST -> createCastComponent(parent, ref);
                case NOT -> createNotComponent(parent, ref);
                case OR, AND, XOR, NOR, NAND, XNOR, CONCAT, ADD, MUL ->
                        createMultiInputComponent(parent, ref, 2);
                case SUB, DIV, MOD ->  createDualInputComponent(parent, ref);
                case EQ, NE, SLT, SLE, SGT, SGE, ULT, ULE, UGT, UGE -> createRelationalOpComponent(parent, ref);
                case SHL, ASHR, LSHR -> createShiftComponent(parent, ref);
            };
        }

        private ComponentScope createRegisterComponent(Scope parent, BuiltinRef ref) {
            Utils.requireEqSize(1, ref.getArgList(),
                    "Register component requires only one boolean argument for clock edge");
            var result = instantiate(parent, ref);
            var attributes = new Attributes();
            moveAttribute(attributes, ref, REG_CLK_EDGE_ATTR, 0, Entities::requiresBooleanValue);
            result.setAttributes(attributes);
            result.addInput(newClock(result));
            result.addInput(newWire(result, "i"));
            result.addOutput(newWire(result, "o"));
            return result;
        }

        private ComponentScope createAsyncRegisterComponent(Scope parent, BuiltinRef ref) {
            Utils.requireEqSize(3, ref.getArgList(),
                    "AsyncRegister component requires three arguments: " +
                             "boolean clock edge, boolean reset edge, integer reset value");
            var result = instantiate(parent, ref);
            var attributes = new Attributes();
            moveAttribute(attributes, ref, REG_CLK_EDGE_ATTR, 0, Entities::requiresBooleanValue);
            moveAttribute(attributes, ref, BuiltinComponent.REG_RST_EDGE_ATTR, 1, Entities::requiresBooleanValue);
            moveAttribute(attributes, ref, BuiltinComponent.REG_RST_VALUE_ATTR, 2, Entities::requiresIntegerValue);
            result.setAttributes(attributes);
            result.addInput(newClock(result));
            result.addInput(newWire(result, "rst", BitType.of(1)));
            result.addInput(newWire(result, "i"));
            result.addOutput(newWire(result, "o"));
            return result;
        }

        private ComponentScope createDupComponent(Scope parent, BuiltinRef ref) {
            Utils.requireEqSize(1, ref.getArgList(),
                    "Dup component requires only one integer argument for number of outputs");
            int dup = requiresIntegerValue(ref.getArgList().get("n", 0)).toInt();

            var component = instantiate(parent, ref);
            var entity = newWire(component, "entity");
            component.addInput(entity);
            for (int i = 0; i < dup; ++i) {
                component.addOutput(entity);
            }
            return component;
        }

        private ComponentScope createSplitComponent(Scope parent, BuiltinRef ref) {
            Utils.requireEqSize(1, ref.getArgList(),
                    "Split component requires only one integer argument for input bit width");
            int split = requiresIntegerValue(ref.getArgList().get("n", 0)).toInt();

            var result = instantiate(parent, ref);
            var entity = newWire(result, "entity", BitType.of(split));
            result.addInput(entity);
            for (int i = split - 1; i >= 0; i--) {
                result.addOutput(Variable.accessOf(entity, Selector.ofBit(i)));
            }
            return result;
        }

        private ComponentScope createCastComponent(Scope parent, BuiltinRef ref) {
            var result = instantiate(parent, ref);
            if (ref.getArgList().isEmpty()) {
                result.addInput(newWire(result, "i"));
                result.addOutput(newWire(result, "o"));
            } else {
                Utils.requireEqSize(1, ref.getArgList(),
                        "Cast component requires one integer argument for bit width");
                int width = requiresIntegerValue(ref.getArgList().get("width", 0)).toInt();
                result.addInput(newWire(result, "i"));
                result.addOutput(newWire(result, "o", BitType.of(width)));
            }
            return result;
        }

        private ComponentScope createRelationalOpComponent(Scope parent, BuiltinRef ref) {
            Utils.requireEqSize(0, ref.getArgList(),
                    "Relational operator component requires no argument");
            var result = instantiate(parent, ref);
            result.addInput(newWire(result, "i1"), newWire(result, "i2"));
            result.addOutput(newWire(result, "o", BitType.forBoolean()));
            return result;
        }

        private ComponentScope createShiftComponent(Scope parent, BuiltinRef ref) {
            Utils.requireEqSize(0, ref.getArgList(),
                    "Shift operator component requires no argument");
            var result = instantiate(parent, ref);
            result.addInput(newWire(result, "i"), newWire(result, "off"));
            result.addOutput(newWire(result, "o"));
            return result;
        }

        private ComponentScope createDualInputComponent(Scope parent, BuiltinRef ref) {
            Utils.requireEqSize(0, ref.getArgList(), ref.getBuiltin() + "requires no argument");
            var result = instantiate(parent, ref);
            result.addInput(newWire(result, "i1"), newWire(result, "i2"));
            result.addOutput(newWire(result, "o"));
            return result;
        }

        private ComponentScope createMultiInputComponent(Scope parent, BuiltinRef ref, int argNumHint) {
            int ports;
            if (!ref.getArgList().isEmpty()) {
                Utils.requireEqSize(1, ref.getArgList(),
                        "Multi-input component requires only one integer argument for number of inputs");
                ports = requiresIntegerValue(ref.getArgList().get("n", 0)).toInt();
            } else {
                ports = argNumHint;
            }

            if (ports == 1) {
                return createIdentityComponent(parent, BuiltinRef.of(TO, ref.getSourceLoc()));
            } else {
                var result = instantiate(parent, ref);
                for (int i = 1; i <= ports; i++) {
                    result.addInput(newWire(result, "i" + i));
                }
                result.addOutput(newWire(result, "o"));
                return result;
            }
        }

        private ComponentScope instantiate(Scope parent, BuiltinRef ref) {
            return ComponentScope.newComponent(parent, ref.getBuiltin().asID().setSourceLoc(ref.getSourceLoc()));
        }

        private Wire newWire(ComponentScope result, String name) {
            return newWire(result, name, PolyType.getInstance());
        }

        private Clock newClock(ComponentScope result) {
            var id = Identifier.of("$clk").setSourceLoc(result.getSourceLoc());
            var clock = new Clock.Factory(compiler).create(result, id);
            result.bind(clock.getIdentifier(), clock);
            return clock;
        }

        private Wire newWire(ComponentScope result, String name, Type type) {
            var id = Identifier.of("$" + name).setSourceLoc(result.getSourceLoc());
            var wire = new Wire.Factory(type, compiler).create(result, id);
            result.bind(wire.getIdentifier(), wire);
            return wire;
        }
    }

    private class Connector {
        private Integer argNumHint = null;

        private static boolean needHintForContinuationOf(Expr expr) {
            return switch (expr) {
                case BuiltinRef _ -> true;
                case SequentialExpr e -> needHintForContinuationOf(e.rhs());
                default -> false;
            };
        }

        public void instantiate(Expr type) {
            doInstantiate(type);
        }

        ComponentPort doInstantiate(Expr expr) {
            return switch (expr) {
                case PrimaryExpr primary -> instantiatePrimaryExpr(primary);
                case ParallelExpr parallel -> instantiateParallelExpr(parallel);
                case DecoratedExpr decorated -> instantiateDecoratedExpr(decorated);
                case SequentialExpr sequential -> instantiateSequentialExpr(sequential);
                case PortExpr connection -> {
                    Logger.error(connection.getLoc(), "Unexpected Port Connect Expression {}", connection);
                    throw new CompileCancellationException();
                }
            };
        }

        private ComponentPort instantiateDecoratedExpr(DecoratedExpr expr) {
            var port = doInstantiate(expr.operand());
            return switch (expr.decorator()) {
                case BLOCK_IN -> new ComponentPort(List.of(), port.output);
                case BLOCK_OUT -> new ComponentPort(port.input, List.of());
                case NON_BLOCK -> port;
            };
        }

        private ComponentPort instantiatePrimaryExpr(PrimaryExpr expr) {
            return switch (expr) {
                case ComponentRef ref -> ComponentPort.of(instantiateComponentRef(ref));
                case BuiltinRef ref -> ComponentPort.of(instantiateBuiltinRef(ref));
                case ImportRef ref -> ComponentPort.of(instantiateImportRef(ref));
                case VariableRef ref -> instantiateInstanceRef(ref);
            };
        }

        private ComponentPort instantiateSequentialExpr(SequentialExpr expr) {
            if (expr.rhs() instanceof PortExpr port) {
                return doPortConnection(instantiateScope(expr.lhs()), port, true);
            }
            if (expr.lhs() instanceof PortExpr lhs) {
                if (expr.rhs() instanceof SequentialExpr(Expr mid, PortExpr rhs)) {
                    var midInst = instantiateScope(mid);
                    doPortConnection(midInst, rhs, true);
                    doPortConnection(midInst, lhs, false);
                    return ComponentPort.ofEmpty();
                } else {
                    return doPortConnection(instantiateScope(expr.rhs()), lhs, false);
                }
            }

            // right-hand-side goes first
            ComponentPort lhs, rhs;
            rhs = doInstantiate(expr.rhs());
            if (needHintForContinuationOf(expr.lhs())) {
                argNumHint = rhs.output().size();
            }
            lhs = doInstantiate(expr.lhs());

            int size = Utils.requireEqSize(lhs.input(), rhs.output(),
                    "Sequential connection requires matched port size");
            for (int i = 0; i < size; ++i) {
                scope.assigns().add(lhs.input().get(i), rhs.output().get(i));
            }
            return new ComponentPort(rhs.input(), lhs.output());
        }

        private ComponentPort doPortConnection(ComponentScope inst, PortExpr connection, boolean isInput) {
            var loc = connection.getLoc();
            var instPort = ComponentPort.of(inst);

            Function<ComponentPort, List<Variable>> getter =
                    (ComponentPort port) -> (isInput ? port.output : port.input);

            Map<Mutable, ComponentPort> assigns = new HashMap<>();
            for (var entry : connection.getPorts()) {
                var conn = doInstantiate(entry.expr());
                if (!(getter.apply(conn).size() == 1)) {
                    Logger.error(loc, "Expect exactly one output of {} but got", entry.expr(), conn.output());
                    throw new CompileCancellationException();
                }
                var port = inst.getEvaluator().evaluate(entry.port());
                assigns.put(requiresMutable(port), conn);
            }

            ComponentPort defaultPort = null;
            var port = isInput ? instPort.input() : instPort.output();
            if (!connection.hasDefault()) {
                Utils.requireEqSize(assigns.size(), port,
                        "Port connection requires all ports to be connected or provide a default connection");
            } else {
                defaultPort = doInstantiate(connection.getDefaultExpr());
            }

            for (var variable : port) {
                if (!(variable instanceof Mutable mutable)) {
                    Logger.error("Expected mutable variable but got {}", variable);
                    throw new CompileCancellationException();
                }

                var expr = assigns.getOrDefault(mutable, defaultPort);
                if (isInput) {
                    scope.assigns().add(mutable, getter.apply(expr).getFirst());
                } else {
                    scope.assigns().add(getter.apply(expr).getFirst(), mutable);
                }
            }

            if (isInput) {
                return new ComponentPort(List.of(), instPort.output);
            } else {
                return new ComponentPort(instPort.input, List.of());
            }
        }

        private ComponentPort instantiateParallelExpr(ParallelExpr parallelExpr) {
            List<Variable> output = new LinkedList<>(), input = new LinkedList<>();
            for (var sub : parallelExpr.exprs()) {
                var port = doInstantiate(sub);
                output.addAll(port.output());
                input.addAll(port.input());
            }
            return new ComponentPort(input, output);
        }

        private ComponentPort instantiateInstanceRef(VariableRef ref) {
            var inst = List.of(ref.getInstance());
            if (ref.getArgList() != null) {
                Logger.error("Unexpected attributes in instance reference {}", ref.getInstance());
                throw new CompileCancellationException();
            }
            if (ref.getInstance() instanceof Mutable) {
                return new ComponentPort(inst, inst);
            } else {
                return new ComponentPort(List.of(), inst);
            }
        }

        private ComponentScope instantiateScope(Expr expr) {
            return switch (expr) {
                case ImportRef ref -> instantiateImportRef(ref);
                case BuiltinRef ref -> instantiateBuiltinRef(ref);
                case ComponentRef ref -> instantiateComponentRef(ref);
                default -> {
                    Logger.error("Unexpected expression type {}", expr);
                    throw new CompileCancellationException();
                }
            };
        }

        private ComponentScope instantiateImportRef(ImportRef ref) {
            SourceLoc sourceLoc = ref.getSourceLoc();

            var compiler = repoOf(scope).getManager().dispatchCompiler(sourceLoc, ref);
            var pack = compiler.getPackManager().resolvePackage(sourceLoc, ref.packageRef());
            var component = compiler.elaborateComponent(pack, ref.componentRef(), ref.getArgList());

            if (ref.getArgList() != null) {
                var attributes = from(pack.resolveComponent(ref.componentRef()), ref.getArgList());
                component.setAttributes(attributes);
            }
            component.setParent(scope);
            component.setPackage(scope.getPackage());
            return component;
        }

        private ComponentScope instantiateBuiltinRef(BuiltinRef ref) {
            ComponentScope component;
            if (argNumHint != null) {
                component = factory.createBuiltInComponent(scope, ref, argNumHint);
                argNumHint = null;
            } else {
                component = factory.createBuiltInComponent(scope, ref);
            }
            return component;
        }

        private ComponentScope instantiateComponentRef(ComponentRef ref) {
            var node = ref.getDefinition();
            var component = ref.isInterface()
                    ? ComponentScope.newInterface(scope, ref.getIdentifier(), true)
                    : ComponentScope.newComponent(scope, ref.getIdentifier());

            var attributes = from(node, ref.getArgList());
            for (var entry : attributes.entrySet()) {
                component.bind(entry.getKey(), entry.getValue());
            }
            component.setAttributes(attributes);
            new Elaborator.ComponentElaborator(component).build(node);
            setSignatureFor(ref, component);
            return component;
        }

        private void setSignatureFor(ComponentRef ref, ComponentScope scope) {
            var signature = Signature.of(ref, scope.getInputs(), scope.getOutputs());
            scope.setSignature(signature);
        }

        private record ComponentPort(List<Variable> input, List<Variable> output) {
            public static ComponentPort of(ComponentScope scope) {
                return new ComponentPort(scope.getInputs(), scope.getOutputs());
            }

            public static ComponentPort ofEmpty() {
                return new ComponentPort(List.of(), List.of());
            }
        }
    }
}
