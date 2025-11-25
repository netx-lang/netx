package pascal.netx.compiler.ast;

import pascal.netx.compiler.ast.element.*;
import pascal.netx.compiler.ast.expr.*;
import pascal.netx.compiler.ast.expr.qualifier.BindingQualifier;
import pascal.netx.compiler.ast.expr.qualifier.FilterQualifier;
import pascal.netx.compiler.ast.expr.qualifier.GeneratorQualifier;
import pascal.netx.compiler.ast.expr.selector.BitSelector;
import pascal.netx.compiler.ast.expr.selector.PartSelector;

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

    default void enter(BranchNode node) {
        enterDefault(node);
    }

    default void exit(BranchNode node) {
        exitDefault(node);
    }

    default void enter(BlockNode node) {
        enterDefault(node);
    }

    default void exit(BlockNode node) {
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

    default void enter(DataRestNode node) {
        enterDefault(node);
    }

    default void exit(DataRestNode node) {
        exitDefault(node);
    }

    default void enter(PackageNode node) {
        enterDefault(node);
    }

    default void exit(PackageNode node) {
        exitDefault(node);
    }

    default void enter(ComponentNode node) {
        enterDefault(node);
    }

    default void exit(ComponentNode node) {
        exitDefault(node);
    }

    default void enter(ComponentSigNode node) {
        enterDefault(node);
    }

    default void exit(ComponentSigNode node) {
        exitDefault(node);
    }

    default void enter(InterfaceNode node) {
        enterDefault(node);
    }

    default void exit(InterfaceNode node) {
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

    default void enter(StringExpr node) {
        enterDefault(node);
    }

    default void exit(StringExpr node) {
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

    default void enter(LetExpr node) {
        enterDefault(node);
    }

    default void exit(LetExpr node) {
        exitDefault(node);
    }

    default void enter(PortConnectionExpr node) {
        enterDefault(node);
    }

    default void exit(PortConnectionExpr node) {
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

    default void enter(IdentifierExpr node) {
        enterDefault(node);
    }

    default void exit(IdentifierExpr node) {
        exitDefault(node);
    }

    default void enter(CallSymbolExpr node) {
        enterDefault(node);
    }

    default void exit(CallSymbolExpr node) {
        exitDefault(node);
    }

    default void enter(PackageInfoNode node) {
        enterDefault(node);
    }

    default void exit(PackageInfoNode node) {
        exitDefault(node);
    }

    default void enter(ImportNode node) {
        enterDefault(node);
    }

    default void exit(ImportNode node) {
        exitDefault(node);
    }

    default void enter(EnumNode node) {
        enterDefault(node);
    }

    default void exit(EnumNode node) {
        exitDefault(node);
    }
}
