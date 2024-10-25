package pascal.hardscript.compiler.io.parser;

import org.antlr.v4.runtime.tree.ParseTree;
import pascal.hardscript.compiler.ast.DesignNode;
import pascal.hardscript.compiler.ast.Identifier;
import pascal.hardscript.compiler.ast.Interpolated;
import pascal.hardscript.compiler.ast.Symbol;
import pascal.hardscript.compiler.ast.expr.*;
import pascal.hardscript.compiler.ast.expr.access.AccessExpr;
import pascal.hardscript.compiler.ast.expr.access.BitSelector;
import pascal.hardscript.compiler.ast.expr.access.PartSelector;
import pascal.hardscript.compiler.ast.expr.access.Selector;
import pascal.hardscript.compiler.ast.expr.list.*;
import pascal.hardscript.compiler.ast.module.*;
import pascal.hardscript.compiler.parser.HardScriptParser.*;
import pascal.hardscript.util.exception.ShallNotReachHereError;
import pascal.hardscript.util.exception.TodoException;
import pascal.hardscript.util.lang.BitVector;

public final class HSParser {

    public static DesignNode parseDesign(DesignContext ctx) {
        return DesignNode.of(ctx.module().stream().map(HSParser::parseModule).toList());
    }

    static ModuleNode parseModule(ModuleContext ctx) {
        return ModuleNode.of(
                parseSymbol(ctx.symbol()), parseExpr(ctx.type),
                ctx.element().stream().map(HSParser::parseElement).toList());
    }


    static ElementNode parseElement(ElementContext ctx) {
        return switch (ctx) {
            case InstantiationContext sub -> parseInstantiation(sub);
            case ComponentContext sub -> parseComponent(sub);
            case LoopContext sub -> parseLoop(sub);
            default -> throw new ShallNotReachHereError();
        };
    }

    static InstantiationNode parseInstantiation(InstantiationContext ctx) {
        return InstantiationNode.of(parseExpr(ctx.type), parseExpr(ctx.connection));
    }

    static LoopNode parseLoop(LoopContext ctx) {
        return LoopNode.of(
                ctx.iterator().stream().map(HSParser::parseGeneratorQualifier).toList(),
                ctx.element().stream().map(HSParser::parseElement).toList());
    }

    static ComponentNode parseComponent(ComponentContext ctx) {
        return ComponentNode.of(
                ctx.symbol().stream().map(HSParser::parseSymbol).toList(),
                parseExpr(ctx.type));
    }

    static ExprNode parseExpr(ExpressionContext ctx) {
        return switch (ctx) {
            case SymbolExprContext sub -> SymbolExpr.of(parseSymbol(sub.symbol()));
            case NumberExprContext sub -> parseNumberExpr(sub.number());
            case ListExprContext sub -> parseListExpr(sub.list());
            case UnaryExprContext sub -> parseUnaryExpr(sub);
            case BinaryExprContext sub -> parseBinaryExpr(sub);
            case AccessExprContext sub -> parseAccessExpr(sub);
            case CallExprContext sub -> parseCallExpr(sub);
            case ConditionalExprContext sub -> parseConditionalExpr(sub);
            case ParenExprContext sub -> parseExpr(sub.expression());
            default -> throw new ShallNotReachHereError();
        };
    }

    static ListExpr parseListExpr(ListContext ctx) {
        return switch (ctx) {
            case CommonListContext sub -> CommonList.of(sub.expression().stream().map(HSParser::parseExpr).toList());
            case RangeListContext sub -> RangeList.of(parseExpr(sub.from), parseExpr(sub.to));
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
            case GeneratorContext sub -> parseGeneratorQualifier(sub.iterator());
            case FilterContext sub -> FilterQualifier.of(parseExpr(sub.expression()));
            case LocalBindingContext sub ->
                    BindingQualifier.of(
                            SymbolExpr.of(parseSymbol(sub.symbol())),
                            parseExpr(sub.expression()));
            default -> throw new ShallNotReachHereError();
        };
    }

    static GeneratorQualifier parseGeneratorQualifier(IteratorContext ctx) {
        return GeneratorQualifier.of(
                SymbolExpr.of(parseSymbol(ctx.symbol())),
                parseExpr(ctx.expression()));
    }

    static ExprNode parseUnaryExpr(UnaryExprContext ctx) {
        UnaryExpr.Operator op = switch (ctx.op.getText()) {
            case "+" -> UnaryExpr.Operator.PLUS;
            case "-" -> UnaryExpr.Operator.MINUS;
            case "!" -> UnaryExpr.Operator.NOT;
            case "~" -> UnaryExpr.Operator.BITWISE_NOT;
            default -> throw new ShallNotReachHereError();
        };
        return UnaryExpr.of(op, parseExpr(ctx.expression()));
    }

    static ExprNode parseBinaryExpr(BinaryExprContext ctx) {
        BinaryExpr.Operator op = switch (ctx.op.getText()) {
            case "<>" -> BinaryExpr.Operator.COMBINE;
            case "->" -> BinaryExpr.Operator.MAPS_TO;
            case "**" -> BinaryExpr.Operator.POW;
            case "+" -> BinaryExpr.Operator.ADD;
            case "-" -> BinaryExpr.Operator.SUB;
            case "*" -> BinaryExpr.Operator.MUL;
            case "/" -> BinaryExpr.Operator.DIV;
            case "%" -> BinaryExpr.Operator.MOD;
            case "==" -> BinaryExpr.Operator.EQ;
            case "!=" -> BinaryExpr.Operator.NE;
            case "<" -> BinaryExpr.Operator.LT;
            case ">" -> BinaryExpr.Operator.GT;
            case "<=" -> BinaryExpr.Operator.LE;
            case ">=" -> BinaryExpr.Operator.GE;
            case "&&" -> BinaryExpr.Operator.LOGIC_AND;
            case "||" -> BinaryExpr.Operator.LOGIC_OR;
            case "&" -> BinaryExpr.Operator.AND;
            case "|" -> BinaryExpr.Operator.OR;
            case "^" -> BinaryExpr.Operator.XOR;
            case "^~", "~^" -> BinaryExpr.Operator.XNOR;
            case "<<" -> BinaryExpr.Operator.LOGIC_SHL;
            case ">>" -> BinaryExpr.Operator.LOGIC_SHR;
            case "<<<" -> BinaryExpr.Operator.ARITH_SHL;
            case ">>>" -> BinaryExpr.Operator.ARITH_SHR;
            default -> throw new ShallNotReachHereError();
        };
        return BinaryExpr.of(op, parseExpr(ctx.lhs), parseExpr(ctx.rhs));
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

    static ExprNode parseAccessExpr(AccessExprContext ctx) {
        var base = parseExpr(ctx.expression());
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

    static CallExpr parseCallExpr(CallExprContext ctx) {
        return CallExpr.of(parseSymbol(ctx.symbol()), ctx.expression().stream().map(HSParser::parseExpr).toList());
    }

    static ExprNode parseConditionalExpr(ConditionalExprContext ctx) {
        return ConditionalExpr.of(parseExpr(ctx.cond), parseExpr(ctx.thenBranch), parseExpr(ctx.elseBranch));
    }

    static Symbol parseSymbol(SymbolContext ctx) {
        return switch (ctx) {
            case IdentifierContext sub -> Identifier.of(sub.ID().getText());
            case InterpolatedContext sub -> Interpolated.of(
                    sub.ID().stream().map(ParseTree::getText).toList(),
                    sub.interpolation().stream()
                            .map(InterpolationContext::expression)
                            .map(HSParser::parseExpr).toList());
            default -> throw new ShallNotReachHereError();
        };
    }
}
