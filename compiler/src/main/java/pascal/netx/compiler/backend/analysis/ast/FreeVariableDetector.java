package pascal.netx.compiler.backend.analysis.ast;


import pascal.netx.compiler.model.ast.ASTListener;
import pascal.netx.compiler.model.ast.expr.*;
import pascal.netx.compiler.model.ast.expr.qualifier.BindingQualifier;
import pascal.netx.compiler.model.ast.expr.qualifier.FilterQualifier;
import pascal.netx.compiler.model.ast.expr.qualifier.GeneratorQualifier;
import pascal.netx.util.lang.Identifier;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public final class FreeVariableDetector implements ASTListener {
    private final Set<Identifier> freeVariables = new HashSet<>();
    private final Stack<Identifier> boundVariables = new Stack<>();

    private boolean notBound(Identifier id) {
        for (var bound : boundVariables) {
            if (bound.equals(id)) {
                return false;
            }
        }
        return true;
    }

    public static Set<Identifier> detect(ExprNode expr) {
        var detector = new FreeVariableDetector();
        expr.walk(detector);
        return detector.freeVariables;
    }

    @Override
    public void enter(LetExpr node) {
        var head = node.getBinding().getHead();
        switch (head) {
            case IdentifierExpr id -> boundVariables.push(id.getSymbol());
            case CallExpr call -> {
                boundVariables.push(call.getSymbol());
                for (var arg : call.getArguments()) {
                    if (arg instanceof IdentifierExpr ident) {
                        boundVariables.push(ident.getSymbol());
                    }
                }
            }
        }
    }

    @Override
    public void exit(LetExpr node) {
        boundVariables.pop();
    }

    @Override
    public void enter(IdentifierExpr node) {
        var id = node.getSymbol();
        if (notBound(id)) {
            freeVariables.add(id);
        }
    }

    @Override
    public void enter(CallExpr node) {
        var id = node.getSymbol();
        if (notBound(id)) {
            freeVariables.add(id);
        }
    }

    @Override
    public void enter(BindingQualifier node) {
        boundVariables.push(node.getBinding().getHead().getSymbol());
    }

    @Override
    public void enter(GeneratorQualifier node) {
        boundVariables.push(node.getHead());
    }

    @Override
    public void exit(ListComprehension node) {
        for (var qualifier : node.getQualifiers()) {
            switch (qualifier) {
                case BindingQualifier _,
                     GeneratorQualifier _ -> boundVariables.pop();
                case FilterQualifier _ -> {
                    // Do nothing
                }
            }
        }
    }

    @Override
    public void exit(NamedPortExpr node) {
        for (var assign : node.getPorts()) {
            if (assign.lhs() != null) {
                freeVariables.removeAll(detect(assign.lhs()));
            }
        }
    }
}
