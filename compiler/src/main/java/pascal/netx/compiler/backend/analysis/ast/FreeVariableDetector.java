package pascal.netx.compiler.backend.analysis.ast;

import pascal.netx.compiler.model.ast.ASTListener;
import pascal.netx.compiler.model.ast.ASTNode;
import pascal.netx.compiler.model.ast.element.BindingNode;
import pascal.netx.compiler.model.ast.expr.*;
import pascal.netx.compiler.model.ast.expr.qualifier.BindingQualifier;
import pascal.netx.compiler.model.ast.expr.qualifier.FilterQualifier;
import pascal.netx.compiler.model.ast.expr.qualifier.GeneratorQualifier;
import pascal.netx.util.lang.Identifier;

import java.util.HashSet;
import java.util.Set;

public final class FreeVariableDetector implements ASTListener {
    private final Set<Identifier> freeVariables = new HashSet<>();
    // Scoped subtrees are analyzed separately, rather than visited again by AST.walk.
    private int skippedDepth;

    public static Set<Identifier> detect(ExprNode expr) {
        var detector = new FreeVariableDetector();
        expr.walk(detector);
        return detector.freeVariables;
    }

    public static Set<Identifier> detect(BindingNode binding) {
        var frees = detect(binding.getExpr());
        if (binding.getHead() instanceof CallExpr call) {
            frees.remove(call.getSymbol());
            for (var arg : call.getArguments()) {
                frees.remove(((IdentifierExpr) arg).getSymbol());
            }
        }
        // An ordinary let does not bind its own name in the initializer.
        return frees;
    }

    @Override
    public void enterDefault(ASTNode node) {
        if (skippedDepth > 0) {
            skippedDepth++;
            return;
        }
        switch (node) {
            case IdentifierExpr id -> freeVariables.add(id.getSymbol());
            case CallExpr call -> freeVariables.add(call.getSymbol());
            case LetExpr let -> {
                freeVariables.addAll(detect(let.getBinding()));
                var body = detect(let.getBody());
                body.remove(let.getBinding().getHead().getSymbol());
                freeVariables.addAll(body);
                skippedDepth = 1;
            }
            case ListComprehension list -> {
                var bound = new HashSet<Identifier>();
                for (var qualifier : list.getQualifiers()) {
                    switch (qualifier) {
                        case GeneratorQualifier generator -> {
                            addUnbound(detect(generator.getFrom()), bound);
                            bound.add(generator.getHead());
                        }
                        case BindingQualifier binding -> {
                            addUnbound(detect(binding.getBinding()), bound);
                            bound.add(binding.getBinding().getHead().getSymbol());
                        }
                        case FilterQualifier filter -> addUnbound(detect(filter.getCondition()), bound);
                    }
                }
                addUnbound(detect(list.getHead()), bound);
                skippedDepth = 1;
            }
            case NamedPortExpr ports -> {
                // The entire LHS is a port selector resolved in the target component.
                // Only RHS expressions belong to the macro's lexical environment.
                for (var assign : ports.getPorts()) {
                    freeVariables.addAll(detect(assign.rhs()));
                }
                skippedDepth = 1;
            }
            default -> {
                // Ordinary expressions inherit the current environment.
            }
        }
    }

    private void addUnbound(Set<Identifier> names, Set<Identifier> bound) {
        names.removeAll(bound);
        freeVariables.addAll(names);
    }

    @Override
    public void exitDefault(ASTNode node) {
        if (skippedDepth > 0) {
            skippedDepth--;
        }
    }
}
