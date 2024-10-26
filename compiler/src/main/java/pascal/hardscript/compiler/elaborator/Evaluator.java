package pascal.hardscript.compiler.elaborator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pascal.hardscript.compiler.ast.Identifier;
import pascal.hardscript.compiler.ast.Interpolated;
import pascal.hardscript.compiler.ast.Symbol;
import pascal.hardscript.compiler.ast.expr.*;
import pascal.hardscript.compiler.ast.expr.qualifier.BindingQualifier;
import pascal.hardscript.compiler.ast.expr.qualifier.FilterQualifier;
import pascal.hardscript.compiler.ast.expr.qualifier.GeneratorQualifier;
import pascal.hardscript.compiler.ast.expr.qualifier.Qualifier;
import pascal.hardscript.compiler.exception.CompileCancellationException;
import pascal.hardscript.compiler.model.Entity;
import pascal.hardscript.compiler.model.ListEntity;
import pascal.hardscript.compiler.model.Wildcard;
import pascal.hardscript.compiler.model.circuit.Component;
import pascal.hardscript.compiler.model.circuit.ComponentAccess;
import pascal.hardscript.compiler.model.circuit.selector.BitSelector;
import pascal.hardscript.compiler.model.circuit.selector.PartSelector;
import pascal.hardscript.compiler.model.circuit.selector.Selector;
import pascal.hardscript.compiler.model.scope.Binding;
import pascal.hardscript.compiler.model.scope.Scope;
import pascal.hardscript.compiler.model.value.BitVectorValue;
import pascal.hardscript.compiler.model.value.BooleanValue;
import pascal.hardscript.compiler.model.value.IntValue;
import pascal.hardscript.compiler.model.value.Value;
import pascal.hardscript.util.exception.ShallNotReachHereError;
import pascal.hardscript.util.exception.TodoException;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.IntStream;

public final class Evaluator {
    private static final Logger log = LoggerFactory.getLogger(Evaluator.class);
    private final LinkedList<Binding> bindings = new LinkedList<>();

    public Evaluator(Scope scope) {
        bindings.add(scope.getBinding());
    }

    public String evaluateToString(ExprNode expr) {
        throw new TodoException();
    }

    public IntValue evaluateToInteger(ExprNode expr) {
        var result = evaluate(expr);
        if (result instanceof IntValue intValue) {
            return intValue;
        } else {
            log.error("{}: Expected integer expression but got {}", expr.getSourceLoc(), result);
            throw new CompileCancellationException();
        }
    }

    public BooleanValue evaluateToBoolean(ExprNode expr) {
        var result = evaluate(expr);
        if (result instanceof BooleanValue booleanValue) {
            return booleanValue;
        } else {
            log.error("{}: Expected boolean expression but got {}", expr.getSourceLoc(), result);
            throw new CompileCancellationException();
        }
    }

    public ListEntity evaluateToList(ExprNode expr) {
        var result = evaluate(expr);
        if (!(result instanceof ListEntity list)) {
            log.error("{}: Expected list expression but got {}", expr.getSourceLoc(), result);
            throw new CompileCancellationException();
        }
        return list;
    }

    public ListEntity evaluateToValueList(ExprNode expr) {
        var result = evaluateToList(expr);
        if (result.entities().stream().anyMatch(Predicate.not(Value.class::isInstance))) {
            log.error("{}: Expected list of meta values but got {}", expr.getSourceLoc(), result);
            throw new CompileCancellationException();
        }
        return result;
    }

    public Entity evaluate(ExprNode expr) {
        return switch (expr) {
            case AccessExpr sub -> evaluateAccessExpr(sub);
            case BinaryExpr sub -> evaluateBinaryExpr(sub);
            case ConditionalExpr sub -> evaluateConditionalExpr(sub);
            case ListExpr sub -> evaluateListExpr(sub);
            case NumberExpr sub -> evaluateNumberExpr(sub);
            case ParamSymbolExpr sub -> evaluateParamSymbolExpr(sub);
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
                    BitSelector.of(evaluateToInteger(bitSelector.getIndex()));
            case pascal.hardscript.compiler.ast.expr.selector.PartSelector partSelector -> PartSelector.of(
                    evaluateToInteger(partSelector.getStart()),
                    evaluateToInteger(partSelector.getEnd()));
        };
    }

    private Entity evaluateListExpr(ListExpr expr) {
        if (expr instanceof WildcardList) {
            return Wildcard.getInstance();
        }

        var list = switch (expr) {
            case CommonList sub -> evaluateCommonList(sub);
            case RangeList sub -> evaluateRangeList(sub);
            case ListComprehension sub -> evaluateListComprehension(sub);
            default -> throw new ShallNotReachHereError();
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
        var from = evaluateToInteger(list.getFrom()).value();
        var to = evaluateToInteger(list.getTo()).value();
        return ListEntity.of(
                IntStream.range(Integer.min(from, to), Integer.max(from, to))
                        .mapToObj(IntValue::of)
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
                    var id = evaluateSymbol(generator.getHead());
                    var list = evaluateToValueList(generator.getFrom());
                    for (var value : list.entities()) {
                        overrideBindings(Binding.of(id, value));
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
                case BindingQualifier binding -> {
                    var id = evaluateSymbol(binding.getHead());
                    var value = evaluate(binding.getExpr());
                    overrideBindings(Binding.of(id, value));
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
            case IntNumber intNumber -> IntValue.of(intNumber.getValue());
        };
    }

    private Entity evaluateUnaryExpr(UnaryExpr expr) {
        throw new TodoException(); // TODO:
    }

    private Entity evaluateBinaryExpr(BinaryExpr expr) {
        throw new TodoException(); // TODO:
    }

    private Entity evaluateParamSymbolExpr(ParamSymbolExpr expr) {
        throw new TodoException(); // TODO:
    }

    private Entity evaluateSymbolExpr(SymbolExpr expr) {
        var id = evaluateSymbol(expr.getSymbol());
        for (var binding : bindings) {
            var entity = binding.lookup(id);
            if (entity != null) {
                return entity;
            }
        }
        log.error("{}: Unresolved Symbol {}", expr.getSourceLoc(), id);
        throw new CompileCancellationException();
    }

    public Identifier evaluateSymbol(Symbol symbol) {
        return switch (symbol) {
            case Identifier id -> id;
            case Interpolated interpolated -> evaluateInterpolation(interpolated);
        };
    }

    private Identifier evaluateInterpolation(Interpolated interpolated) {
        var sb = new StringBuilder();
        var exprIter = interpolated.exprs().iterator();
        for (var fragment : interpolated.fragments()) {
            sb.append(fragment);
            sb.append(evaluateToString(exprIter.next()));
        }
        exprIter.forEachRemaining(expr -> sb.append(evaluateToString(expr)));
        var result = Identifier.of(sb.toString());
        result.setSourceLoc(interpolated.getSourceLoc());
        return result;
    }

    private void overrideBindings(Binding binding) {
        bindings.addFirst(binding);
    }

    private void restoreBindings() {
        bindings.removeFirst();
    }
}
