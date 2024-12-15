package pascal.hardscript.compiler.io;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;
import pascal.hardscript.compiler.antlr.HardScriptParser.*;
import pascal.hardscript.compiler.ast.ASTListener;
import pascal.hardscript.compiler.ast.ASTNode;
import pascal.hardscript.compiler.ast.DesignNode;
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
import pascal.hardscript.util.lang.Identifier;
import pascal.hardscript.util.lang.SourceLoc;

import java.util.ArrayList;
import java.util.List;

public final class HSParser {

    public static DesignNode parseDesign(DesignContext ctx) {
        DesignNode result = DesignNode.of().setSourceLoc(locationOf(ctx.getStart()));
        for (var child : ctx.children) {
            switch (child) {
                case Binding_statementContext sub -> result.addDefinition(parseBinding(sub.binding()));
                case ComponentContext sub -> result.addDefinition(parseComponent(sub));
                default -> throw new ShallNotReachHereError();
            }
        }
        return result;
    }

    static ComponentNode parseComponent(ComponentContext ctx) {
        ComponentNode.Kind kind = switch (ctx.kind.getText()) {
            case "component" -> ComponentNode.Kind.COMPONENT;
            case "interface" -> ComponentNode.Kind.INTERFACE;
            default -> throw new ShallNotReachHereError();
        };
        var attributes = parseComponentAttributes(ctx);
        var elements = parseElements(ctx.element());

        ComponentTypeNode type = switch (kind) {
            case COMPONENT -> parseComponentType(ctx.component_type(), locationOf(ctx.getStart()));
            case INTERFACE -> {
                var port = parseExpr(ctx.type);
                yield ComponentTypeNode.of(port, port).setSourceLoc(locationOf(ctx.type.getStart()));
            }
        };

        return ComponentNode.of(kind, parseIdentifier(ctx.ID()), type, attributes, elements)
                .setSourceLoc(locationOf(ctx.getStart()));
    }

    static List<Identifier> parseComponentAttributes(ComponentContext ctx) {
        if (ctx.attributes() == null) {
            return List.of();
        }
        return ctx.attributes().ID().stream()
                .map(HSParser::parseIdentifier).toList();
    }

    static ComponentTypeNode parseComponentType(Component_typeContext ctx, SourceLoc sourceLoc) {
        if (ctx == null) {
            var result = ComponentTypeNode.of(CommonList.ofEmpty(), CommonList.ofEmpty());
            result.walk(new ASTListener() {
                @Override
                public void enterDefault(ASTNode node) {
                    node.setSourceLoc(sourceLoc);
                }
            });
            return result;
        } else {
            return ComponentTypeNode.of(parseExpr(ctx.from), parseExpr(ctx.to))
                    .setSourceLoc(locationOf(ctx.getStart()));
        }
    }

    static ElementNode parseElement(ElementContext ctx) {
        var result = switch (ctx) {
            case BindingStatementContext sub -> parseBinding(sub.binding_statement().binding());
            case MixedInstantiationContext sub -> parseMixedInstantiation(sub);
            case DataDeclarationContext sub -> parseDataInstantiation(sub);
            case ComponentInstantiationContext sub -> parseComponentInstantiation(sub);
            case LoopContext sub -> parseLoop(sub);
            default -> throw new ShallNotReachHereError();
        };
        return result.setSourceLoc(locationOf(ctx.getStart()));
    }

    static ComponentInstantiationNode parseComponentInstantiation(ComponentInstantiationContext ctx) {
        return ComponentInstantiationNode.of(parseExpr(ctx.expression()), false);
    }

    static ComponentInstantiationNode parseMixedInstantiation(MixedInstantiationContext ctx) {
        return ComponentInstantiationNode.of(parseExpr(ctx.expression()), true);
    }

    static LoopNode parseLoop(LoopContext ctx) {
        return LoopNode.of(parseGeneratorQualifier(ctx.iterator()), parseElements(ctx.element()));
    }

    static DataInstantiationNode parseDataInstantiation(DataDeclarationContext ctx) {
        var decl = ctx.data_declaration();
        SymbolExpr type;
        DataInstantiationNode.Kind kind;
        var declarations = parseDeclarationList(decl.declaration_list());
        switch (decl.kind.getText()) {
            case "wire" -> {
                kind = DataInstantiationNode.Kind.WIRE;
                type = parseSymbolExpr(decl.type);
            }
            case "bundle" -> {
                kind = DataInstantiationNode.Kind.BUNDLE;
                type = parseSymbolExpr(decl.type);
            }
            case "auto" -> {
                kind = DataInstantiationNode.Kind.WIRE;
                type = null;
            }
            default -> throw new ShallNotReachHereError();
        }
        return DataInstantiationNode.of(kind, declarations, type);
    }

    static List<DataRestNode> parseDeclarationList(Declaration_listContext ctx) {
        List<DataRestNode> result = new ArrayList<>();
        for (var sub : ctx.declaration_symbol()) {
            DataRestNode data = DataRestNode.of(
                    parseIdentifier(sub.ID()),
                    sub.expression().stream().map(HSParser::parseExpr).toList()
            ).setSourceLoc(locationOf(sub.getStart()));
            result.add(data);
        }
        return result;
    }

    static ExprNode parseExpr(ExpressionContext ctx) {
        var result = switch (ctx) {
            case ListExprContext sub -> parseListExpr(sub);
            case UnaryExprContext sub -> parseUnaryExpr(sub);
            case FieldExprContext sub -> parseFieldExpr(sub);
            case SymbolExprContext sub -> parseSymbolExpr(sub);
            case NumberExprContext sub -> parseNumberExpr(sub);
            case AccessExprContext sub -> parseAccessExpr(sub);
            case BinaryExprContext sub -> parseBinaryExpr(sub);
            case ConditionalExprContext sub -> parseConditionalExpr(sub);
            case ParenExprContext sub -> parseExpr(sub.expression());
            default -> throw new ShallNotReachHereError();
        };
        return result.setSourceLoc(locationOf(ctx.getStart()));
    }

    static SymbolExpr parseSymbolExpr(SymbolExprContext ctx) {
        return parseSymbolExpr(ctx.symbol());
    }

    static SymbolExpr parseSymbolExpr(SymbolContext ctx) {
        return switch (ctx) {
            case PrimarySymbolContext sub -> parsePrimarySymbol(sub);
            case ParamSymbolContext sub -> parseParamSymbolExpr(sub);
            default -> throw new ShallNotReachHereError();
        };
    }

    static ListExpr parseListExpr(ListExprContext ctx) {
        return switch (ctx.list()) {
            case CommonListContext sub -> CommonList.of(parseExprList(sub.expression()));
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
        return result.setSourceLoc(locationOf(ctx.getStart()));
    }

    static GeneratorQualifier parseGeneratorQualifier(IteratorContext ctx) {
        return GeneratorQualifier.of(
                parseIdentifier(ctx.ID()),
                parseExpr(ctx.expression())
        ).setSourceLoc(locationOf(ctx.getStart()));
    }

    static BindingNode parseBinding(BindingContext ctx) {
        return BindingNode.of(parseSymbolExpr(ctx.symbol()), parseExpr(ctx.expression()))
                .setSourceLoc(locationOf(ctx.getStart()));
    }

    static ExprNode parseUnaryExpr(UnaryExprContext ctx) {
        UnaryExpr.Operator op;
        if (ctx.prev != null) {
            op = UnaryExpr.Operator.SINK;
        } else if (ctx.post != null) {
            op = UnaryExpr.Operator.SOURCE;
        } else {
            op = UnaryExpr.Operator.fromString(ctx.op.getText());
        }
        return UnaryExpr.of(op, parseExpr(ctx.expression()));
    }

    static ExprNode parseBinaryExpr(BinaryExprContext ctx) {
        var op = BinaryExpr.Operator.fromString(ctx.op.getText());
        return BinaryExpr.of(op, parseExpr(ctx.lhs), parseExpr(ctx.rhs));
    }

    static NumberExpr parseNumberExpr(NumberExprContext ctx) {
        return switch (ctx.number()) {
            case IntNumberContext sub -> IntNumber.of(Integer.parseUnsignedInt(sub.UNSIGNED_INTEGER().getText()));
            case BoolNumberContext sub -> BoolNumber.of(Boolean.parseBoolean(sub.BOOLEAN().getText()));
            case BitVectorNumberContext sub -> parseBitVectorNumber(sub);
            default -> throw new ShallNotReachHereError();
        };
    }

    static BitVectorNumber parseBitVectorNumber(BitVectorNumberContext ctx) {
        var literal = ctx.getText().toLowerCase().split("'");
        int size = Integer.parseInt(literal[0]);
        int radix = switch (literal[1].charAt(0)) {
            case 'b' -> 2;
            case 'o' -> 8;
            case 'd' -> 10;
            case 'h' -> 16;
            default -> throw new ShallNotReachHereError();
        };
        return BitVectorNumber.of(BitVector.parse(literal[1].substring(1), size, radix));
    }

    static ExprNode parseFieldExpr(FieldExprContext ctx) {
        return FieldExpr.of(
                parseExpr(ctx.expression()),
                parseIdentifier(ctx.ID()));
    }

    static ExprNode parseAccessExpr(AccessExprContext ctx) {
        var result = parseExpr(ctx.expression());
        for (var selector : ctx.selector()) {
            result = AccessExpr.of(result, parseSelector(selector)).setSourceLoc(result.getSourceLoc());
        }
        return result;
    }

    static Selector parseSelector(SelectorContext ctx) {
        var result = switch (ctx) {
            case BitSelectorContext sub -> BitSelector.of(parseExpr(sub.expression()));
            case PartSelectorContext sub -> PartSelector.of(parseExpr(sub.from), parseExpr(sub.to));
            default -> throw new ShallNotReachHereError();
        };
        return result.setSourceLoc(locationOf(ctx.getStart()));
    }

    static ParamSymbolExpr parseParamSymbolExpr(ParamSymbolContext ctx) {
        return ParamSymbolExpr.of(
                parseIdentifier(ctx.ID()),
                parseExprList(ctx.expression())
        ).setSourceLoc(locationOf(ctx.getStart()));
    }

    static ExprNode parseConditionalExpr(ConditionalExprContext ctx) {
        return ConditionalExpr.of(parseExpr(ctx.cond), parseExpr(ctx.thenBranch), parseExpr(ctx.elseBranch));
    }

    static Identifier parseIdentifier(TerminalNode ID) {
        return Identifier.of(ID.getText()).setSourceLoc(locationOf(ID.getSymbol()));
    }

    static IdentifierExpr parsePrimarySymbol(PrimarySymbolContext ctx) {
        return IdentifierExpr.of(parseIdentifier(ctx.ID())).setSourceLoc(locationOf(ctx.getStart()));
    }

    static List<ElementNode> parseElements(List<ElementContext> list) {
        List<ElementNode> result = new ArrayList<>();
        for (var element : list) {
            result.add(parseElement(element));
        }
        return result;
    }

    static List<ExprNode> parseExprList(List<ExpressionContext> list) {
        List<ExprNode> result = new ArrayList<>();
        for (var expr : list) {
            result.add(parseExpr(expr));
        }
        return result;
    }

    static SourceLoc locationOf(Token token) {
        return new SourceLoc(token.getLine(), token.getCharPositionInLine());
    }
}
