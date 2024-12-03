package pascal.hardscript.compiler.ast;

import pascal.hardscript.compiler.ast.component.*;
import pascal.hardscript.compiler.ast.expr.*;
import pascal.hardscript.compiler.ast.expr.qualifier.BindingQualifier;
import pascal.hardscript.compiler.ast.expr.qualifier.FilterQualifier;
import pascal.hardscript.compiler.ast.expr.qualifier.GeneratorQualifier;
import pascal.hardscript.compiler.ast.expr.selector.BitSelector;
import pascal.hardscript.compiler.ast.expr.selector.PartSelector;

public interface ASTListener {

    default void enterDefault(ASTNode node) {
        // do nothing
    }

    default void exitDefault(ASTNode node) {
        // do nothing
    }

    default void enter(FilterQualifier node) {
        enterDefault(node);
    }

    default void exit(FilterQualifier node) {
        exitDefault(node);
    }

    default void enter(BindingQualifier node) {
        enterDefault(node);
    }

    default void exit(BindingQualifier node) {
        exitDefault(node);
    }

    default void enter(GeneratorQualifier node) {
        enterDefault(node);
    }

    default void exit(GeneratorQualifier node) {
        exitDefault(node);
    }

    default void enter(ComponentInstantiationNode node) {
        enterDefault(node);
    }

    default void exit(ComponentInstantiationNode node) {
        exitDefault(node);
    }

    default void enter(LoopNode node) {
        enterDefault(node);
    }

    default void exit(LoopNode node) {
        exitDefault(node);
    }

    default void enter(BindingNode node) {
        enterDefault(node);
    }

    default void exit(BindingNode node) {
        exitDefault(node);
    }

    default void enter(DataInstantiationNode node) {
        enterDefault(node);
    }

    default void exit(DataInstantiationNode node) {
        exitDefault(node);
    }

    default void enter(DesignNode node) {
        enterDefault(node);
    }

    default void exit(DesignNode node) {
        exitDefault(node);
    }

    default void enter(ComponentNode node) {
        enterDefault(node);
    }

    default void exit(ComponentNode node) {
        exitDefault(node);
    }

    default void enter(ComponentTypeNode node) {
        enterDefault(node);
    }

    default void exit(ComponentTypeNode node) {
        exitDefault(node);
    }

    default void enter(BitSelector node) {
        enterDefault(node);
    }

    default void exit(BitSelector node) {
        exitDefault(node);
    }

    default void enter(PartSelector node) {
        enterDefault(node);
    }

    default void exit(PartSelector node) {
        exitDefault(node);
    }

    default void enter(IntNumber node) {
        enterDefault(node);
    }

    default void exit(IntNumber node) {
        exitDefault(node);
    }

    default void enter(BoolNumber node) {
        enterDefault(node);
    }

    default void exit(BoolNumber node) {
        exitDefault(node);
    }

    default void enter(BitVectorNumber node) {
        enterDefault(node);
    }

    default void exit(BitVectorNumber node) {
        exitDefault(node);
    }

    default void enter(UnaryExpr node) {
        enterDefault(node);
    }

    default void exit(UnaryExpr node) {
        exitDefault(node);
    }

    default void enter(BinaryExpr node) {
        enterDefault(node);
    }

    default void exit(BinaryExpr node) {
        exitDefault(node);
    }

    default void enter(ConditionalExpr node) {
        enterDefault(node);
    }

    default void exit(ConditionalExpr node) {
        exitDefault(node);
    }

    default void enter(RangeList node) {
        enterDefault(node);
    }

    default void exit(RangeList node) {
        exitDefault(node);
    }

    default void enter(ListComprehension node) {
        enterDefault(node);
    }

    default void exit(ListComprehension node) {
        exitDefault(node);
    }

    default void enter(CommonList node) {
        enterDefault(node);
    }

    default void exit(CommonList node) {
        exitDefault(node);
    }

    default void enter(AccessExpr node) {
        enterDefault(node);
    }

    default void exit(AccessExpr node) {
        exitDefault(node);
    }

    default void enter(FieldExpr node) {
        enterDefault(node);
    }

    default void exit(FieldExpr node) {
        exitDefault(node);
    }

    default void enter(IdentifierSymbol node) {
        enterDefault(node);
    }

    default void exit(IdentifierSymbol node) {
        exitDefault(node);
    }

    default void enter(InterpolatedSymbol node) {
        enterDefault(node);
    }

    default void exit(InterpolatedSymbol node) {
        exitDefault(node);
    }

    default void enter(ParamSymbolExpr node) {
        enterDefault(node);
    }

    default void exit(ParamSymbolExpr node) {
        exitDefault(node);
    }
}
