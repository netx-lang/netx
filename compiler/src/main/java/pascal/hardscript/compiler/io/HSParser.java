package pascal.hardscript.compiler.io;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import pascal.hardscript.compiler.antlr.HardScriptParser.*;
import pascal.hardscript.compiler.ast.DesignNode;
import pascal.hardscript.compiler.ast.Identifier;
import pascal.hardscript.compiler.ast.SourceLoc;
import pascal.hardscript.compiler.ast.component.*;
import pascal.hardscript.compiler.ast.expr.*;
import pascal.hardscript.compiler.ast.expr.qualifier.BindingQualifier;
import pascal.hardscript.compiler.ast.expr.qualifier.FilterQualifier;
import pascal.hardscript.compiler.ast.expr.qualifier.GeneratorQualifier;
import pascal.hardscript.compiler.ast.expr.qualifier.Qualifier;
import pascal.hardscript.compiler.ast.expr.selector.BitSelector;
import pascal.hardscript.compiler.ast.expr.selector.PartSelector;
import pascal.hardscript.compiler.ast.expr.selector.Selector;
import pascal.hardscript.util.exception.ShallNotReachHereError;
import pascal.hardscript.util.lang.BitVector;

import java.util.LinkedList;
import java.util.List;

public final class HSParser {

    public static DesignNode parseDesign(DesignContext ctx) {
        return DesignNode.of(ctx.component().stream().map(HSParser::parseComponent).toList())
                .setSourceLoc(SourceLoc.from(ctx.getStart()));
    }

    static ComponentNode parseComponent(ComponentContext ctx) {
        List<Identifier> arguments = ctx.component_arguments() != null
                ? ctx.component_arguments().ID().stream().map(HSParser::parseIdentifier).toList()
                : List.of();
        return ComponentNode.of(parseIdentifier(ctx.ID()),
                        parseComponentType(SourceLoc.from(ctx.ID().getSymbol()), ctx.component_type()),
                        arguments, ctx.element().stream().map(HSParser::parseElement).toList())
                .setSourceLoc(SourceLoc.from(ctx.getStart()));
    }

    static ComponentTypeNode parseComponentType(SourceLoc sourceLoc, Component_typeContext ctx) {
        if (ctx == null) {
            ListExpr from = CommonList.ofEmpty().setSourceLoc(sourceLoc);
            ListExpr to = CommonList.ofEmpty().setSourceLoc(sourceLoc);
            return ComponentTypeNode.of(from, to).setSourceLoc(sourceLoc);
        } else {
            return ComponentTypeNode.of(parseExpr(ctx.from), parseExpr(ctx.to))
                    .setSourceLoc(SourceLoc.from(ctx.getStart()));
        }
    }

    static ElementNode parseElement(ElementContext ctx) {
        var result = switch (ctx) {
            case BindingStatementContext sub -> parseBinding(sub.binding());
            case DataInstantiationContext sub -> parseDataInstantiationNode(sub);
            case ComponentInstantiationContext sub -> parseComponentInstantiation(sub);
            case LoopContext sub -> parseLoop(sub);
            default -> throw new ShallNotReachHereError();
        };
        return result.setSourceLoc(SourceLoc.from(ctx.getStart()));
    }

    static ComponentInstantiationNode parseComponentInstantiation(ComponentInstantiationContext ctx) {

        return ComponentInstantiationNode.of(parseExpr(ctx.lhs), parseExpr(ctx.rhs));
    }

    static LoopNode parseLoop(LoopContext ctx) {
        return LoopNode.of(
                parseGeneratorQualifier(ctx.iterator()),
                ctx.element().stream().map(HSParser::parseElement).toList());
    }

    static DataInstantiationNode parseDataInstantiationNode(DataInstantiationContext ctx) {
        return DataInstantiationNode.of(
                ctx.primary_symbol().stream().map(HSParser::parsePrimarySymbol).toList(), parseTypeExpr(ctx.type));
    }

    static ExprNode parseTypeExpr(Type_expressionContext ctx) {
        var arguments = new LinkedList<ExprNode>();
        for (var arg : ctx.type_argument()) {
            if (arg.expression() != null) {
                arguments.add(parseExpr(arg.expression()));
            } else {
                arguments.add(null);
            }
        }
        return ParamSymbolExpr.of(parsePrimarySymbol(ctx.primary_symbol()), arguments)
                .setSourceLoc(SourceLoc.from(ctx.getStart()));
    }

    static ExprNode parseExpr(ExpressionContext ctx) {
        var result = switch (ctx) {
            case SymbolExprContext sub -> parseSymbolExpr(sub.symbol());
            case NumberExprContext sub -> parseNumberExpr(sub.number());
            case ListExprContext sub -> parseListExpr(sub.list());
            case UnaryExprContext sub -> parseUnaryExpr(sub);
            case BinaryExprContext sub -> parseBinaryExpr(sub);
            case AccessExprContext sub -> parseAccessExpr(sub);
            case ConditionalExprContext sub -> parseConditionalExpr(sub);
            case LambdaExprContext sub -> parseLambdaExpr(sub);
            case ParenExprContext sub -> parseExpr(sub.expression());
            default -> throw new ShallNotReachHereError();
        };
        return result.setSourceLoc(SourceLoc.from(ctx.getStart()));
    }

    static SymbolExpr parseSymbolExpr(SymbolContext ctx) {
        return switch (ctx) {
            case PrimarySymbolExprContext sub -> parsePrimarySymbol(sub.primary_symbol());
            case ParamSymbolExprContext sub -> parseParamSymbolExpr(sub);
            default -> throw new ShallNotReachHereError();
        };
    }

    static ListExpr parseListExpr(ListContext ctx) {
        return switch (ctx) {
            case CommonListContext sub -> CommonList.of(sub.expression().stream().map(HSParser::parseExpr).toList());
            case RangeListContext sub -> RangeList.of(parseExpr(sub.from), parseExpr(sub.to));
            case ListComprehensionContext sub -> parseListComprehension(sub);
            case EmptyListContext ignored -> CommonList.ofEmpty();
            default -> throw new ShallNotReachHereError();
        };
    }

    static ListComprehension parseListComprehension(ListComprehensionContext ctx) {
        return ListComprehension.of(
                parseExpr(ctx.expression()),
                ctx.qualifier().stream().map(HSParser::parserQualifier).toList());
    }

    static Qualifier parserQualifier(QualifierContext ctx) {
        var result = switch (ctx) {
            case GeneratorContext sub -> parseGeneratorQualifier(sub.iterator());
            case FilterContext sub -> FilterQualifier.of(parseExpr(sub.expression()));
            case LocalBindingContext sub -> BindingQualifier.of(parseBinding(sub.binding()));
            default -> throw new ShallNotReachHereError();
        };
        return result.setSourceLoc(SourceLoc.from(ctx.getStart()));
    }

    static GeneratorQualifier parseGeneratorQualifier(IteratorContext ctx) {
        return GeneratorQualifier.of(parsePrimarySymbol(ctx.primary_symbol()), parseExpr(ctx.expression()))
                .setSourceLoc(SourceLoc.from(ctx.getStart()));
    }

    static BindingNode parseBinding(BindingContext ctx) {
        return BindingNode.of(parsePrimarySymbol(ctx.primary_symbol()), parseExpr(ctx.expression()))
                .setSourceLoc(SourceLoc.from(ctx.getStart()));
    }

    static ExprNode parseUnaryExpr(UnaryExprContext ctx) {
        UnaryExpr.Operator op = switch (ctx.op.getText()) {
            case "+" -> UnaryExpr.Operator.ADD;
            case "-" -> UnaryExpr.Operator.SUB;
            case "!" -> UnaryExpr.Operator.NOT;
            case "~" -> UnaryExpr.Operator.BITWISE_NOT;
            default -> throw new ShallNotReachHereError();
        };
        return UnaryExpr.of(op, parseExpr(ctx.expression()));
    }

    static ExprNode parseBinaryExpr(BinaryExprContext ctx) {
        BinaryExpr.Operator op = switch (ctx.op.getText()) {
            case "<>" -> BinaryExpr.Operator.COMBINE;
            case "::" -> BinaryExpr.Operator.CONCAT;
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
            case BoolNumberContext sub -> BoolNumber.of(Boolean.parseBoolean(sub.BOOLEAN().getText()));
            case BitVectorNumberContext sub -> parseBitVectorNumber(sub);
            default -> throw new ShallNotReachHereError();
        };
    }

    static BitVectorNumber parseBitVectorNumber(BitVectorNumberContext ctx) {
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
        var result = switch (ctx) {
            case BitSelectorContext sub -> BitSelector.of(parseExpr(sub.expression()));
            case PartSelectorContext sub -> PartSelector.of(
                    parseExpr(sub.expression(0)), parseExpr(sub.expression(1)));
            default -> throw new ShallNotReachHereError();
        };
        return result.setSourceLoc(SourceLoc.from(ctx.getStart()));
    }

    static ParamSymbolExpr parseParamSymbolExpr(ParamSymbolExprContext ctx) {
        return ParamSymbolExpr.of(
                parsePrimarySymbol(ctx.primary_symbol()),
                ctx.expression().stream().map(HSParser::parseExpr).toList());
    }

    static ExprNode parseConditionalExpr(ConditionalExprContext ctx) {
        return ConditionalExpr.of(parseExpr(ctx.cond), parseExpr(ctx.thenBranch), parseExpr(ctx.elseBranch));
    }

    static ExprNode parseLambdaExpr(LambdaExprContext ctx) {
        return LambdaExpr.of(
                ctx.primary_symbol().stream().map(HSParser::parsePrimarySymbol).toList(),
                parseExpr(ctx.expression()));
    }

    static Identifier parseIdentifier(TerminalNode ID) {
        return Identifier.of(ID.getText()).setSourceLoc(SourceLoc.from(ID.getSymbol()));
    }

    static PrimarySymbol parsePrimarySymbol(Primary_symbolContext ctx) {
        var result = switch (ctx) {
            case IdentifierContext sub -> IdentifierSymbol.of(parseIdentifier(sub.ID()));
            case InterpolatedContext sub -> InterpolatedSymbol.of(
                    sub.ID().stream().map(ParseTree::getText).toList(),
                    sub.interpolation().stream()
                            .map(InterpolationContext::expression)
                            .map(HSParser::parseExpr).toList());
            default -> throw new ShallNotReachHereError();
        };
        return result.setSourceLoc(SourceLoc.from(ctx.getStart()));
    }
}
