package pascal.hardscript.compiler.io.parser;

import pascal.hardscript.compiler.ast.DesignNode;
import pascal.hardscript.compiler.ast.Identifier;
import pascal.hardscript.compiler.ast.expr.*;
import pascal.hardscript.compiler.ast.expr.access.AccessExpr;
import pascal.hardscript.compiler.ast.expr.access.BitSelector;
import pascal.hardscript.compiler.ast.expr.access.PartSelector;
import pascal.hardscript.compiler.ast.expr.access.Selector;
import pascal.hardscript.compiler.ast.expr.atom.*;
import pascal.hardscript.compiler.ast.expr.list.*;
import pascal.hardscript.compiler.ast.module.*;
import pascal.hardscript.compiler.ast.type.CompTypeNode;
import pascal.hardscript.compiler.ast.type.CompositeCompType;
import pascal.hardscript.compiler.ast.type.PrimitiveCompType;
import pascal.hardscript.compiler.parser.HardScriptParser.*;
import pascal.hardscript.util.exception.ShallNotReachHereError;
import pascal.hardscript.util.exception.TodoException;
import pascal.hardscript.util.lang.BitVector;

import java.util.Map;

public final class HSParser {

    public static DesignNode parseDesign(DesignContext ctx) {
        return DesignNode.of(ctx.module().stream().map(HSParser::parseModule).toList());
    }

    static ModuleNode parseModule(ModuleContext ctx) {
        return ModuleNode.of(Identifier.of(ctx.ID()),
                parseModuleType(ctx.module_type()),
                ctx.element().stream().map(HSParser::parseElement).toList());
    }

    static ModuleTypeNode parseModuleType(Module_typeContext ctx) {
        return ModuleTypeNode.of(
                parseListExpr(ctx.list_expression(0)),
                parseListExpr(ctx.list_expression(1)));
    }

    static ElementNode parseElement(ElementContext ctx) {
        return switch (ctx) {
            case InstantiationWrapperContext sub -> parseInstantiation(sub.instantiation());
            case ComponentContext sub -> parseComponent(sub);
            case LoopContext sub -> parseLoop(sub);
            default -> throw new ShallNotReachHereError();
        };
    }

    static InstantiationNode parseInstantiation(InstantiationContext ctx) {
        return InstantiationNode.of(
                ctx.instantiation_ref().stream().map(HSParser::parseInstantiationRef).toList(),
                ctx.list_expression().stream().map(HSParser::parseListExpr).toList());
    }

    static InstRef parseInstantiationRef(Instantiation_refContext ctx) {
        return switch (ctx) {
            case AtomInstRefContext sub -> AtomInstRef.of(Identifier.of(sub.ID()));
            case CompositeInstRefContext sub -> CompositeInstRef.of(parseListExpr(sub.list_expression()));
            default -> throw new ShallNotReachHereError();
        };
    }

    static LoopNode parseLoop(LoopContext ctx) {
        return LoopNode.of(
                ctx.iterator().stream().map(HSParser::parseGeneratorQual).toList(),
                ctx.element().stream().map(HSParser::parseElement).toList());
    }

    static ComponentNode parseComponent(ComponentContext ctx) {
        return ComponentNode.of(
                ctx.ID().stream().map(Identifier::of).toList(),
                parseComponentType(ctx.component_type()));
    }

    static CompTypeNode parseComponentType(Component_typeContext ctx) {
        return switch (ctx) {
            case PrimitiveCompTypeContext sub -> PrimitiveCompType.of(parseAtomExpr(sub.atom_expression()));
            case CompositeCompTypeContext sub -> CompositeCompType.of(Identifier.of(sub.ID()),
                    sub.component_type().stream().map(HSParser::parseComponentType).toList());
            default -> throw new ShallNotReachHereError();
        };
    }

    static ExprNode parseExpr(ExpressionContext ctx) {
        return switch (ctx) {
            case PrimaryExprWrapperContext sub -> parsePrimaryExpr(sub.primary_expression());
            case AccessExprWrapperContext sub -> parseAccessExpr(sub.access_expression());
            case ConditionalExprWrapperContext sub -> parseConditionalExpr(sub.conditional_expression());
            case ParenExprContext sub -> parseExpr(sub.expression());
            default -> throw new ShallNotReachHereError();
        };
    }

    static ExprNode parsePrimaryExpr(Primary_expressionContext ctx) {
        return switch (ctx) {
            case ListExprContext sub -> parseListExpr(sub.list_expression());
            case AtomExprWrapperContext sub -> parseAtomExpr(sub.atom_expression());
            case InstExprContext sub -> InstExpr.of(parseInstantiation(sub.instantiation()));
            default -> throw new ShallNotReachHereError();
        };
    }

    static AtomExpr parseAtomExpr(Atom_expressionContext ctx) {
        return switch (ctx) {
            case NumberExprContext sub -> parseNumberExpr(sub.number());
            case IdentifierContext sub -> IdentifierExpr.of(Identifier.of(sub.ID()));
            default -> throw new ShallNotReachHereError();
        };
    }

    static ListExpr parseListExpr(List_expressionContext ctx) {
        return switch (ctx) {
            case CommonListContext sub -> CommonList.of(sub.expression().stream().map(HSParser::parseExpr).toList());
            case RangeListContext sub -> RangeList.of(
                    parseAtomExpr(sub.atom_expression(0)),
                    parseAtomExpr(sub.atom_expression(1)));
            case EmptyListContext ignored -> CommonList.ofEmpty();
            case ListComprehensionContext sub -> parseListComprehension(sub);
            default -> throw new ShallNotReachHereError();
        };
    }

    static ListComprehension parseListComprehension(ListComprehensionContext ctx) {
        return ListComprehension.of(
                parseExpr(ctx.expression()),
                ctx.qualifier().stream().map(HSParser::parserQualifier).toList());
    }

    static Qualifier parserQualifier(QualifierContext ctx) {
        return switch (ctx) {
            case GeneratorContext sub -> parseGeneratorQual(sub.iterator());
            case FilterContext sub -> FilterQual.of(parseExpr(sub.expression()));
            case LocalBindingContext sub -> BindingQual.of(Identifier.of(sub.ID()), parseExpr(sub.expression()));
            default -> throw new ShallNotReachHereError();
        };
    }

    static GeneratorQual parseGeneratorQual(IteratorContext ctx) {
        return GeneratorQual.of(Identifier.of(ctx.ID()), parseListExpr(ctx.list_expression()));
    }

    static NumberExpr parseNumberExpr(NumberContext ctx) {
        return switch (ctx) {
            case IntNumberContext sub -> IntNumber.of(Integer.parseInt(sub.INTEGER().getText()));
            case BitVectorNumberWrapperContext sub -> parseBitVectorNumber(sub.bitvector_number());
            default -> throw new TodoException("Numeric Literals");
        };
    }

    static BitVectorNumber parseBitVectorNumber(Bitvector_numberContext ctx) {
        var literal = ctx.getText().toLowerCase().split("'");
        int size = Integer.parseInt(literal[0]);
        int radix = switch (literal[1].charAt(0)){
            case 'b' -> 2;
            case 'o' -> 8;
            case 'd' -> 10;
            case 'h' -> 16;
            default -> throw new ShallNotReachHereError();
        };
        return BitVectorNumber.of(BitVector.parse(literal[1].substring(1), size, radix));
    }

    static ExprNode parseAccessExpr(Access_expressionContext ctx) {
        var base = parsePrimaryExpr(ctx.primary_expression());
        return ctx.selector().stream()
                .map(HSParser::parseSelector)
                .reduce(base, AccessExpr::of, (e, _) -> e);
    }

    static Selector parseSelector(SelectorContext ctx) {
        return switch (ctx) {
            case BitSelectorContext sub -> BitSelector.of(parseExpr(sub.expression()));
            case PartSelectorContext sub -> PartSelector.of(
                    parseExpr(sub.expression(0)), parseExpr(sub.expression(1)));
            default -> throw new ShallNotReachHereError();
        };
    }

    static ExprNode parseConditionalExpr(Conditional_expressionContext ctx) {
        return ConditionalExpr.of(
                parseAtomExpr(ctx.atom_expression()),
                parseExpr(ctx.expression(0)), parseExpr(ctx.expression(1)));
    }
}
